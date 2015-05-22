//
//  StringReader.swift
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

import Foundation

class StringReader {
    
    var string:String
    var index = 0
    
    init(string:String){
        self.string = string
    }
    
    func readline() -> String {
        var line = ""
        while index < count(string.utf16) {
            var char =  string[index++]
            if char == "\n" {
                break
            }
            line += char
        }
        return line
    }
    
    func read() -> String {
        var char = "\000"
        if index < count(string.utf16) {
            char =  string[index++]
        }
        return char
    }
    
}

extension String {
    subscript (i: Int) -> String {
        return String(Array(self)[i])
    }
}
