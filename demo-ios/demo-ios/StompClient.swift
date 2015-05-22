//
//  StompClient.swift
//  demo-ios
//
//  Created by xuduo on 12/11/14.
//  Copyright (c) 2014 yy. All rights reserved.
//

import Foundation

public protocol StompDelegate: class {
    func stompDidConnect()
}


class StompClient : WebSocketDelegate {
    
    var webSocket:WebSocket
    var seq = 0
    var requestHandlers:[String:(JSON)->()] = [:]
    var subscribeHandlers:[String:(JSON)->()] = [:]
    weak var delegate : StompDelegate?
    
    
    init(scheme: String, host: String, path:String){
        webSocket = WebSocket(url: NSURL(scheme: scheme, host: host, path: path)!)
    }
    
    func connect() {
        webSocket.delegate = self
        webSocket.connect()
        println("open socket")
    }
    
    func subscribe(destination:String,handler:(JSON)->() = {(json:JSON)->() in }) {
        write("SUBSCRIBE", headers: ["destination":destination,"id": "sub" + String(seq++)])
        subscribeHandlers[destination] = handler
    }
    
    internal func write(command:String, headers :[String:String] = [:], payload:String = ""){
        webSocket.writeString(Message.toMessageString(command, headers: headers,  payload: payload))
    }
    
    func request(destination:String, payload:AnyObject,successHandler:(JSON)->()) {
        let json = JSON(payload)
        let raw = json.rawString()!
        let requestId = String(seq++)
        requestHandlers[requestId] = successHandler
        write("SEND",headers:["request-id":requestId,"destination":destination],payload:raw)
  
    }
    
   internal func websocketDidConnect() {
        println("websocket is connected")
        webSocket.writeString(Message.toMessageString("CONNECT", headers: [:],  payload: ""))
    }
    
    internal func websocketDidDisconnect(error: NSError?) {
        println("websocket is disconnected")
    }
    
    internal func websocketDidWriteError(error: NSError?) {
        println("wez got an error from the websocket: \(error!.localizedDescription)")
    }
    
    internal func websocketDidReceiveMessage(text: String) {
        println("got some text: \(text)")
        var stomp = Message.parse(text)
        println("parseMessage \n\(stomp.command)")
        
        if(stomp.command == "CONNECTED"){
            subscribe("/user/queue/reply")
            delegate?.stompDidConnect()
        } else if(stomp.command == "MESSAGE"){
            let destination  = stomp.destination()
            if(destination == "/user/queue/reply"){
                let requestId = stomp.headers["request-id"]
                let handler = requestHandlers.removeValueForKey(requestId!)
                let json = JSON(stomp.payload)
                handler?(json)
            } else {
                let handler = subscribeHandlers[destination]
                let json = JSON(data:stomp.payload.dataUsingEncoding(NSUTF8StringEncoding)!)
                handler?(json)
            }
        }
    }
    
    internal func websocketDidReceiveData(data: NSData) {
        println("got some data: \(data.length)")
    }
    
    
    
}