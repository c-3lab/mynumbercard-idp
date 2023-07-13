//
//  MessageController.swift
//  MyNumberCardAuth
//
//  Created by abelstaff on 2023/07/04.
//

import Foundation

class MessageController: ObservableObject {
    @Published var isAlert:Bool = false
    @Published var messageTitle:String = ""
    @Published var messageString:String = ""
}
