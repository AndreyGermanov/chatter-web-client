package core
import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.files.FileReader
import utils.*
import kotlin.*
import kotlin.browser.window
import kotlin.js.Date

/**
 * Wrapper for WebSocket server with queue support. Client puts request to send message to queue. Each message
 * must have unique request_id field and "sender" field with link to object, which should receive response to
 * this request. After receiving request, object sends it to server and moves it to other queue -
 * requestsWaitingResponses and wait until server responds. When receive response, which has request_id,
 * MessageCenter extracts request with this response_id from requestsWaitingResponses queue and executes
 * handleWebSocketResponse(request_id,response) of sender object, connected to this waiting request. So,
 * this object must implement MessageCenterResponseListener protocol.
 */
object MessageCenter {

    /*****************************
     * WebSocket server core vars
     ****************************/

    // Instance of WebSocket server
    lateinit var ws: WebSocket

    // Name or IP address of websocket server
    lateinit var host: String

    // Port number of websocket server
    var port: Int = 80

    // Endpoint URL of websocket server
    lateinit var endpoint: String

    // Last response, received from socket server as string
    var lastResponse = ""

    // Last response received from server as object
    var lastResponseObject: HashMap<String, Any>? = null

    // Last request sent to webSocket server as string
    var lastRequestText = ""

    // Last request sent to webSocket server as object
    var lastRequestObject: HashMap<String,Any>? = null

    // Last received file from webSocket server
    var lastReceivedFile:ArrayBuffer? = null

    // Timer used to implement queue processing cronjob every second
    var timer: Int = 0

    // User ID, used to authenticate WebSocket requests
    var user_id:String = ""

    // Session ID, used to authenticate WebSocket requests
    var session_id:String = ""

    // Allows to set MessageCenter to work in testing mode and do not implement
    // real connections to server
    var testingMode:Boolean = false

    // For testing mode, allows to set connection status of MessageCenter
    var testingModeConnected:Boolean = false

    // For production mode, determines if connection to WebSocket server established
    var connectionStatus:Boolean = false

    // Determines if Message center connected to server
    var isConnected:Boolean = false
        get() {
           var result:Boolean;
           if (this.testingMode) {
               result = this.testingModeConnected
           } else {
               result = this.connectionStatus
           }
           return result
        }


    /***************************
     * Message queue core vars *
     **************************/

    // Pending requests, indexed by request_id
    var pendingRequests = HashMap<String,Any>()

    // Pending request timeout in seconds, after it will be automatically removed from queue
    var pendingRequestsQueueTimeout = 10

    // Set requests, waiting for response, indexed by request_id
    var requestsWaitingResponse = HashMap<String,Any>()

    // Timeout for request, waiting response in seconds, after it will be automatically removed from queue
    var requestsWaitingResponseQueueTimeout = 20

    // Files, received from WebSocket server, which can be intended for requests, indexed by CRC32 checksum of file
    var receivedFiles = HashMap<Double,Any>()

    // Timeout for received file in seconds. If it does not belong to any request, it will be removed after this timeout
    var receivedFilesQueueTimeout = 120

    // Responses to requests, which already received from servers, but still waiting some binary data,
    // connected to it. These responses will be processed after files received in receivedFilesQueue.
    // It indexed by CRC32 checksum of file and contains response body and request body, with request_id
    var responsesWaitingFile = HashMap<Double,Any>()

    // Timeout for response waiting file. If timeout exceeded and file not received, this response will be removed from
    // queue (seconds)
    var responsesWaitingFileTimeout = 120


    /***************************
     * Message center main loop
     **************************/

    /**
     * Function used to initialize main options of MessageCenter
     *
     * @param host: Host name of WebSocket server
     * @param port: Port of WebSocket server
     * @param endpoint: URL of root of WebSocket server
     */
    fun setup(host:String,port:Int,endpoint:String) {
        this.host = host
        this.port = port
        this.endpoint = endpoint
    }

    /**
     * Function used to construct WebSocketClient object and connect it to server
     */
    fun connect() {
        if (!this.testingMode) {
            this.ws = WebSocket("ws://" + this.host + ":" + this.port + "/" + this.endpoint)

            this.ws.onopen = { event ->
                this.onConnect(event)
            }
            this.ws.onclose = { event ->
                this.onDisconnect(event)
            }
            this.ws.onmessage = { event ->
                this.onMessage(event as MessageEvent)
            }
            this.ws.onerror = { error ->
                this.onError(error)
            }
        }
    }

    /**
     * Main loop function: runs every second, checks server connection, processes and cleans
     * all message queues
     */
    fun loop() {
        if (!this@MessageCenter.isConnected) {
            this@MessageCenter.connect()
        }
        this@MessageCenter.processPendingRequests()
        this@MessageCenter.cleanPendingRequests()
        this@MessageCenter.cleanRequestsWaitingResponses()
        this@MessageCenter.cleanResponsesWaitingFile()
        this@MessageCenter.cleanReceivedFiles()
    }

    /**
     * Starter function. Starts main loop
     */
    fun run() {
        window.setInterval({this.loop()},1000)
    }

    /***************************
     * Message queue management
     **************************/

    /**
     * Adds request to pendingRequests queue
     *
     * @param request: Request
     * @return request_id of added request
     */
    fun addToPendingRequests(request:HashMap<String,Any>): HashMap<String,Any>? {
        var request = request
        if (!request.containsKey("sender") || request["sender"]==null) {
            Logger.log(LogLevel.WARNING,"Could not add request to queue. No 'sender' object specified. "+
            "Request body: ${request}")
            return null
        }
        if (request["sender"] !is MessageCenterResponseListener) {
            Logger.log(LogLevel.WARNING, "Could not add request to queue. Specified 'sender' object is incorrect."+
            "Request body: ${request}")
            return null
        }
        var request_id = guid()
        if (!request.containsKey("request_id")) {
            request["request_id"] = request_id
        }
        val request_timestamp = (Date.now()/1000).toInt()
        if (request.containsKey("request_timestamp")) {
            Logger.log(LogLevel.ERROR,"'request_timestamp' attribute already exists. Could not add this request."+
                    "Request body: ${request}", "MessageCenter","addToPendingRequests")
            return null
        }
        request["request_timestamp"] = request_timestamp
        if (this.user_id.isNotEmpty()) {
            request["user_id"] = this.user_id
        }
        if (this.session_id.isNotEmpty()) {
            request["session_id"] = this.session_id
        }
        Logger.log(LogLevel.DEBUG,"Added request with ${request_id} to pendingRequests queue",
        "MessageCenter","addToPendingRequests")
        this.pendingRequests[request_id] = request
        return request
    }

    /**
     * Removes request from pendingRequests queue
     *
     * @param request_id: request ID to remove
     * @return removed request body if it really removed or nil if nothing removed
     */
    fun removeFromPendingRequests(request_id:String): HashMap<String,Any>? {
        var request = this.pendingRequests[request_id] as? HashMap<String,Any>
        if (request == null) {
            Logger.log(LogLevel.DEBUG, "Could not remove request ${request_id} from pendingRequests, " +
                    "it does not exist", "MessageCenter", "removeFromPendingRequests")
            return null
        }
        Logger.log(LogLevel.DEBUG,"Removing request ${request_id} from pendingRequests",
        "MessageCenter","removeFromPendingRequests")
        this.pendingRequests.remove(request_id)
        return request
    }

    /**
     * Used to send all requests from pendingRequests queue to the server
     * and put them to requetsWaitingResponsesQueue queue
     */
    fun processPendingRequests() {
        for ((request_id,request) in this.pendingRequests) {
            var request = request as? HashMap<String,Any>
            if (request == null) {
                this.removeFromPendingRequests(request_id)
                Logger.log(LogLevel.DEBUG,"Could not send request with id ${request_id}. Incorrect format of" +
                        "request body: ${request}","MessageCenter","processPendingRequests")
                return
            }
            var sender = request["sender"] as? MessageCenterResponseListener
            if (sender  == null) {
                Logger.log(LogLevel.WARNING,"Failed to send message. Incorrect sender",
                        "MessageCenter","processPendingRequests")
                this.removeFromPendingRequests(request_id)
                return
            }
            var message_to_send = HashMap<String,Any>()
            var files_to_send = ArrayList<ArrayBuffer>()
            for ((field_index,field) in request) {
                if (field_index != "sender") {
                    if (field is ArrayBuffer) {
                        files_to_send.add(field as ArrayBuffer)
                    } else {
                        message_to_send[field_index] = field
                    }
                }
            }
            var failed_to_send_message = false
            if (message_to_send.count()>0) {
                try {
                    this.lastRequestText = stringifyJSON(message_to_send)
                } catch (e:Exception) {
                    Logger.log(LogLevel.WARNING,"Failed to send message. Failed to construct JSON " +
                            "from message ${message_to_send}", "MessageCenter","processPendingRequests")
                    sender.handleWebSocketResponse(request_id, hashMapOf("status" to "error",
                            "status_code" to MessageCenterErrorCodes.RESULT_ERROR_REQUEST_PARSE_ERROR,
                            "request" to request) as HashMap<String,Any>)
                    failed_to_send_message = true
                }
            }
            if (failed_to_send_message) {
                Logger.log(LogLevel.WARNING, "Failed to send message. Message is empty",
                        "MessageCenter", "processPendingRequests")
                this.removeFromPendingRequests(request_id)
                return
            }
            this.lastRequestObject = message_to_send
            this.addToRequestsWaitingResponses(request)
            if (this.testingMode) {
                this.removeFromPendingRequests(request_id)
                return
            }
            if (this.isConnected && !this.testingMode) {
                this.ws.send(this.lastRequestText)
                if (files_to_send.count() > 0 && !failed_to_send_message) {
                    for (binary in files_to_send) {
                        this.ws.send(binary)
                    }
                }
                Logger.log(LogLevel.DEBUG,"Sent request to WebSocketServer - "+this.lastRequestText,
                "MessageCenter","processPendingRequests")
                this.removeFromPendingRequests(request_id)
            }
        }

    }

    /**
     * Cleans outdated records from pendingRequests queue, based on 'request_timestamp' attribute
     */
    fun cleanPendingRequests() {
        for ((request_id,request) in this.pendingRequests) {
            val request= request as? HashMap<String,Any>
            if (request == null) {
                this.removeFromPendingRequests(request_id)
                continue
            }
            val timestamp = request["request_timestamp"] as? Int
            if (timestamp == null) {
                this.removeFromPendingRequests(request_id)
                continue
            }
            if ((Date.now() / 1000).toInt() - timestamp >= this.pendingRequestsQueueTimeout) {
                this.removeFromPendingRequests(request_id)
            }
        }
    }

    /**
     * Adds request to requestsWaitingResponsesQueue
     *
     * @param request: Request
     * @return: added request or null if nothing added
     */
    fun addToRequestsWaitingResponses(request:HashMap<String,Any>): HashMap<String,Any>? {
        val request = request
        val request_id = request["request_id"] as? String
        if (request_id != null) {
            val request_timestamp = (Date.now()/1000).toInt()
            request["request_timestamp"] = request_timestamp
            this.requestsWaitingResponse[request_id] = request
            Logger.log(LogLevel.DEBUG,"Adding request with ${request_id} to requestsWaitingResponses queue. " +
                    "Queue content ${this.requestsWaitingResponse}",
            "MessageCenter","addToRequestsWaitingResponses")
            return request
        } else {
            Logger.log(LogLevel.DEBUG,"Could not add request ${request} to requestsWaitingResponses " +
                    "queue.", "MessageCenter","addToRequestsWaitingResponses")
            return null
        }
    }

    /**
     * Removes request from requestsWaitingResponses
     *
     * @param request_id: Request ID to remove or nil of nothing removed
     * @return body of removed request
     */
    fun removeFromRequestsWaitingResponses(request_id: String): HashMap<String,Any>? {
        val request = this.requestsWaitingResponse[request_id] as? HashMap<String,Any>
        if (request != null) {
            Logger.log(LogLevel.DEBUG,"Removing request ${request_id} from requestsWaitingResponses queue",
            "MessageCenter","removeFromRequestsWaitingResponses")
            this.requestsWaitingResponse.remove(request_id)
            return request
        } else {
            return null
        }
    }

    /**
     * Removes outdated records from requestsWaitingResponses queue
     */
    fun cleanRequestsWaitingResponses() {
        for ((request_id,request) in this.requestsWaitingResponse) {
            val request = request as? HashMap<String,Any>
            if (request == null) {
                this.removeFromRequestsWaitingResponses(request_id)
                continue
            }
            val timestamp = request["request_timestamp"] as? Int
            if (timestamp == null) {
                this.removeFromRequestsWaitingResponses(request_id)
                continue
            }
            if ((Date.now()/1000).toInt()-timestamp >= this.requestsWaitingResponseQueueTimeout) {
                this.removeFromRequestsWaitingResponses(request_id)
            }
        }
    }

    /**
     * Adds record to receivedFiles queue, keyed by checksum of file and marked by timestamp of
     * a moment when file was added
     *
     * @param data: Binary data of file
     * @return added record
     */
    fun addToReceivedFiles(data: ArrayBuffer): HashMap<String,Any> {
        val checksum = crc32FromArrayBuffer(data)
        val timestamp = (Date.now()).toInt()
        val record:HashMap<String,Any> = hashMapOf("data" to data,"timestamp" to timestamp)
        Logger.log(LogLevel.DEBUG,"Added file with checksum ${checksum} to receivedFiles queue",
        "MessageCenter","addToReceivedFiles")
        this.receivedFiles[checksum] = record
        return record
    }

    /**
     * Removes record from receivedFiles queue
     *
     * @param checksum: Checksum of file to remove
     * @return record of removed file or nil if no record removed
     */
    fun removeFromReceivedFiles(checksum:Double): HashMap<String,Any>? {
        val record = this.receivedFiles[checksum] as? HashMap<String,Any>
        if (record != null){
            Logger.log(LogLevel.DEBUG,"Removing file with $checksum from receivedFiles",
            "MessageCenter","removeFromReceivedFiles")
            this.receivedFiles.remove(checksum)
            return record
        } else {
            return null
        }
    }

    /**
     * Removes outdated files from receivedFiles queue
     */
    fun cleanReceivedFiles() {
        for ((checksum,_) in this.receivedFiles) {
            val record = this.receivedFiles[checksum] as? HashMap<String,Any>
            if (record == null) {
                this.removeFromReceivedFiles(checksum)
                continue
            }
            val timestamp = record["timestamp"] as? Int
            if (timestamp == null) {
                this.removeFromReceivedFiles(checksum)
                continue
            }
            if ((Date.now()/1000).toInt()-timestamp >= this.receivedFilesQueueTimeout) {
                this.removeFromReceivedFiles(checksum)
            }
        }
    }

    /**
     * Function adds record to responsesWaitingFile queue
     *
     * @param checksum: Checksum of file, which response is wating
     * @param response: Body of response, which is waiting this file
     * @return added record
     */
    fun addToResponsesWaitingFile(checksum:Double,response:HashMap<String,Any>): HashMap<String,Any> {
        val timestamp = (Date.now()/1000).toInt()
        val record = mapOf("response" to response,"timestamp" to timestamp) as HashMap<String,Any>
        Logger.log(LogLevel.DEBUG,"Added file with checksum ${checksum} to responsesWaitingFile queue",
        "MessageCenter","addToResponsesWaitingFile")
        this.responsesWaitingFile[checksum] = record
        return record
    }

    /**
     * Function removes record from responsesWaitingFile queue
     *
     * @param checksum: Checksum of record to remove
     * @return removed record or nil if nothing removed
     */
    fun removeFromResponsesWaitingFile(checksum:Double): HashMap<String,Any>? {
        val record = this.responsesWaitingFile[checksum] as? HashMap<String,Any>
        if (record != null) {
            Logger.log(LogLevel.DEBUG,"Removing file with ${checksum} from responsesWaitingFile queue",
            "MessageCenter","removeFromResponsesWaitingFile")
            this.responsesWaitingFile.remove(checksum)
            return record
        } else {
            return null
        }
    }

    /**
     * Function removes outdated records from responsesWaitingFile queue
     */
    fun cleanResponsesWaitingFile() {
        for ((checksum,_) in this.responsesWaitingFile) {
            val record = this.responsesWaitingFile[checksum] as? HashMap<String,Any>
            if (record == null) {
                this.removeFromResponsesWaitingFile(checksum)
                continue
            }
            val timestamp = record["timestamp"] as? Int
            if (timestamp == null) {
                this.removeFromResponsesWaitingFile(checksum)
                continue
            }
            if ((Date.now()/1000).toInt() >= this.receivedFilesQueueTimeout) {
                this.removeFromResponsesWaitingFile(checksum)
            }
        }
    }

    /***************************
     * WebSocket event handlers
     **************************/

    /**
     * WebSocket server connection handler. Called when connection to server established
     *
     * @param event: Connection event information
     */
    fun onConnect(event: Event) {
        this@MessageCenter.connectionStatus = true
        Logger.log(LogLevel.DEBUG,"WebSocket server connection established",
                "MessageCenter","onConnect")
    }

    /**
     * WebSocket server disconnection handler. Called when connection to server terminated
     *
     * @param event: Event desciption
     */
    fun onDisconnect(event: Event) {
        this@MessageCenter.connectionStatus = false
        Logger.log(LogLevel.DEBUG,"WebSocket server connection terminated. Reason:${event.target}")
    }

    /**
     * WebSocket server message handler. Called when received response from WebSocket server
     *
     * @param event: Event which includes information about response, including received response
     * body in event.data property. Depending on type of received message body (Text or Binary) it called
     * one of specific methods, implemented below
     */
    fun onMessage(event:MessageEvent) {
        if (event.data is String) {
            var text = event.data.toString()
            Logger.log(LogLevel.DEBUG,"Received response from WebSocket Server. Response body: " +
                    "${text}","MessageCenter","onMessage"
            )
            this.onTextMessage(text)
        } else if (event.data is Blob) {
            var data = event.data as Blob
            var reader = FileReader()
            reader.onloadend = { it
                if (it.target == null) {
                    Logger.log(LogLevel.WARNING, "Could not parse received binary data from WebSocket server",
                            "MessageCenter", "onMessage")
                } else {
                    var target:dynamic = it.target
                    var binary = target.result as? ArrayBuffer
                    if (binary != null) {
                        this@MessageCenter.onDataMessage(binary)
                    } else {
                        Logger.log(LogLevel.WARNING, "Could not parse received binary data from WebSocket server",
                                "MessageCenter", "onMessage")
                    }
                }
            }
            reader.readAsArrayBuffer(data)
            Logger.log(LogLevel.DEBUG,"Received binary data response from WebSocket server",
                    "MessageCenter", "onMessage")
        }
    }

    /**
     * Function called to handle text response from WebSocket server. If it has correct format and
     * contains "sender" object, function passes it to handler method of this sender object
     *
     * @param text: Text of received message
     */
    fun onTextMessage(text:String) {
        Logger.log(LogLevel.DEBUG,"Received text response from WebSocket Server. Response body: " +
                "$text","MessageCenter","onTextMessage")
        if (text.isEmpty()) {
            Logger.log(LogLevel.WARNING,"Received empty text response from WebSocket Server.",
                    "MessageCenter","onTextMessage")
            return
        }
        var response = parseJSON(text)
        if (response == null || jsTypeOf(response) == "undefined") {
            Logger.log(LogLevel.WARNING,"Incorrect JSON text response received. Response body: " +
                    "$text","MessageCenter","onTextMessage")
            return
        }
        var i = 0;

        val request_id = response["request_id"] as? String
        if (request_id == null || request_id.length == 0) {
            Logger.log(LogLevel.WARNING,"No request_id in received text response. Response body: " +
                    "$text","MessageCenter","onTextMessage")
            return
        }
        val request = this.requestsWaitingResponse[request_id] as? HashMap<String,Any>
        if (request == null) {
            Logger.log(LogLevel.WARNING,"Request with request_id=$request_id not found in " +
                    "requestsWaitingResponse queue. Response body: $text",
                    "MessageCenter","onTextMessage")
            return
        }
        val sender = request["sender"] as? MessageCenterResponseListener
        if (sender == null) {
            Logger.log(LogLevel.WARNING,"Response for request with request_id=$request_id does not " +
                    "have correct 'sender' linked. Response body: $text",
                    "MessageCenter","onTextMessage")
            return
        }
        Logger.log(LogLevel.DEBUG,"Run handler for request $request_id for incoming text message $text",
        "MessageCenter","websocketDidReceiveMessage")
        response["request"] = request
        sender.handleWebSocketResponse(request_id,response)
    }

    /**
     * Function called when response with binary data received from WebSocket server. Function places it
     * to "receivedFiles" queue. Then, if there are some requests, which received text responses, but still
     * waiting this binary date, function passes this data, along with request_id to handler of object, which is
     * waiting this file
     *
     * @param data: received binary data, transformed to ArrayBuffer
     */
    fun onDataMessage(data:ArrayBuffer) {
        val checksum = crc32FromArrayBuffer(data)
        Logger.log(LogLevel.DEBUG,"Received binary data with checskum: $checksum from WebSocket server",
                "MessageCenter",
                "onDataMessage")
        this.addToReceivedFiles(data)
        val responseObj = this.responsesWaitingFile[checksum] as? HashMap<String,Any>
        if (responseObj == null) {
            Logger.log(LogLevel.WARNING,"Could not find waiting response for file with checksum $checksum in " +
                    "responsesWaitingFile queue", "MessageCenter", "onDataMessage")
            return
        }
        val response = responseObj["response"] as? HashMap<String,Any>
        if (response == null) {
            Logger.log(LogLevel.WARNING,"Could not find link to response object in record of responsesWaitingFile " +
                    "with checksum $checksum","MessageCenter", "onDataMessage")
            return
        }
        val request = response["request"] as? HashMap<String,Any>
        if (request == null) {
            Logger.log(LogLevel.WARNING,"Could not find link to request object in record of responsesWaitingFile " +
                    "with checksum $checksum","MessageCenter", "onDataMessage")
            return
        }
        val request_id = request["request_id"] as? String
        if (request_id == null) {
            Logger.log(LogLevel.WARNING,"Could not find request_id in record of responsesWaitingFile " +
                    "with checksum $checksum","MessageCenter", "onDataMessage")
            return
        }
        val sender = request["sender"] as? MessageCenterResponseListener
        if (sender == null) {
            Logger.log(LogLevel.WARNING,"Could not find link to request sender in record of responsesWaitingFile " +
                    "with checksum $checksum","MessageCenter", "onDataMessage")
            return
        }
        Logger.log(LogLevel.DEBUG,"Run hander for request $request_id for incoming binary data",
        "MessageCenter","websocketDidReceiveData")
        sender.handleWebSocketResponse(request_id,response)
    }

    /**
     * WebSocket error handler. Called on error when processing WebSocket requests or responses
     *
     * @param err: Error description event
     */
    fun onError(err:Event) {
        Logger.log(LogLevel.ERROR,"Error in WebSocket server operation ${err.toString()}",
                "MessageCenter","onError")
    }
}

/**
 *  Message Center error definitions
 */
enum class MessageCenterErrorCodes(name:String) {
    RESULT_ERROR_REQUEST_PARSE_ERROR("Could not encode request to send to server")
}

/**
 * Interface which should be implemented by any object, which needs to receive
 * and process responses from MessageCenter
 */
interface MessageCenterResponseListener {
    /**
     * Method, which websocket server called on "sender" object when receives response
     * to request.
     *
     * @param request_id : Request id for which response received
     * @param response: Received response body
     */
    fun handleWebSocketResponse(request_id:String,response:HashMap<String,Any>)
}