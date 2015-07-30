//
//  ViewController.swift
//  demo-ios
//
//  Created by xuduo on 12/11/14.
//  Copyright (c) 2014 yy. All rights reserved.
//

import UIKit
import CoreFoundation

class ViewController: UIViewController , StompDelegate{

//    required init(coder aDecoder: NSCoder) {
//        fatalError("init(coder:) has not been implemented")
//    }
//    
   var stomp = StompClient(scheme: "http", host: "mlbs.yy.com:8080", path: "/stomp")
    
    @IBOutlet weak var username: UITextField!
    @IBOutlet weak var password: UITextField!
    @IBOutlet weak var time: UILabel!
    
    @IBAction func login(sender: AnyObject) {
        //stomp.writeString(Message.toMessageString("SEND", headers: [:],  payload: ""))
//        stomp.request("/login", payload: ["username": username.text,"password":password.text],successHandler:{(json:JSON?)->() in
//            println("login success \(json)")
//        })
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        stomp.connect()
        stomp.delegate = self
        println("open socket")
    }
    
    func stompDidConnect() {
//        stomp.subscribe("/topic/time", handler: { (json:JSON?) -> () in
//            let value = json?["message"].stringValue
//            println("qqqqq \(json)")
//            self.time.text = json?["message"].stringValue
//        })
    }
   
}

