//
//  Message.swift
//  demo-ios
//
//  Created by xuduo on 12/11/14.
//  Copyright (c) 2014 yy. All rights reserved.
//

import Foundation

class Message {
    
    var command = ""
    var headers = [String: String]()
    var payload = ""
    
    func destination() -> String {
        return headers["destination"]!
    }
    
    class func toMessageString(command:String,headers:[String:String],payload:String) -> String{
        var result = command + "\n"
        
        if(headers.count > 0){
            for (key,value) in headers {
                result += key + ":" + value + "\n"
            }
        }
        
        result += "\n"
        
        result += payload
        
        result += "\0"
        
        println("toMessageString\n\(result)")
        
        return result
        
    }
    
    class func parse(result:String) -> Message {
        println("Message.parse")
        var message = Message()
        var reader = StringReader(string: result)
        message.command = reader.readline()
        
        var header = ""
        while true {
          header = reader.readline()
            println("header readLine + \n\(header)")
          if header == "" || header == "\0" {
                break
           }
          var array = header.componentsSeparatedByString(":")
          message.headers[array[0]] = array[1]
        }
        
        
        
        while true {
            var char = reader.read()
            if(char == "\0"){
                break
            }
            message.payload += char
        }
        
        return message
    }
    

    
}