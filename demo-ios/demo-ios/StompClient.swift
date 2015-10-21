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
    
    func requestInfo(destination:String, payload:AnyObject,successHandler:(JSON)->()) {
        let json = JSON(payload)
        let raw = json.rawString()!
        let requestId = String(seq++)
        requestHandlers[requestId] = successHandler
        write("SEND",headers:["requestInfo-id":requestId,"destination":destination],payload:raw)
  
    }
    
//    func websocketDidConnect(socket: WebSocket)
//    func websocketDidDisconnect(socket: WebSocket, error: NSError?)
//    func websocketDidReceiveMessage(socket: WebSocket, text: String)
//    func websocketDidReceiveData(socket: WebSocket, data: NSData)
    
    func websocketDidConnect(socket: WebSocket) {
        println("websocket is connected")
        webSocket.writeString(Message.toMessageString("CONNECT", headers: [:],  payload: ""))
    }
    
    func websocketDidDisconnect(socket: WebSocket,error: NSError?) {
        println("websocket is disconnected")
    }
    
    func websocketDidWriteError(socket: WebSocket,error: NSError?) {
        println("wez got an error from the websocket: \(error!.localizedDescription)")
    }
    
    func websocketDidReceiveMessage(socket: WebSocket,text: String) {
        //var text = NSString(data:data,encoding:NSUTF8StringEncoding) as! String
        println("got some text: \(text)")
        var stomp = Message.parse(text)
        println("parseMessage \n\(stomp.command)")
        
        if(stomp.command == "CONNECTED"){
            subscribe("/user/queue/reply")
            delegate?.stompDidConnect()
        } else if(stomp.command == "MESSAGE"){
            let destination  = stomp.destination()
            if(destination == "/user/queue/reply"){
                let requestId = stomp.headers["requestInfo-id"]
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
    
    func websocketDidReceiveData(socket: WebSocket,data: NSData) {
        println("got some data: \(data.length)")
    }
    
    
    
}