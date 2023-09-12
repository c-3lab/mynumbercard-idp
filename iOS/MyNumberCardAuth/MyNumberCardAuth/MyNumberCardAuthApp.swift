//
//  MyNumberCardAuthApp.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/04/20.
//

import SwiftUI

enum Mode {
    case Login         // ログイン
    case Registration  // 登録
    case Replacement   // 変更
    
    func isMode(queryDict : [String: String]) -> Self {
        if(((queryDict["mode"]) != nil)){
            // 登録時
            if ((queryDict["mode"]) == "registration"){
                return .Registration
            }
            // 変更時
            if ((queryDict["mode"]) == "replacement"){
                return .Replacement
            }
        }
        
        return .Login
    }
}

enum ShowView {
    case UserVerificationView // 利用者証明用電子証明書読込View
    case SignatureView        // 署名用電子証明書読込View
    case ExplanationView      // アプリ説明画面
    
    func isViewMode(queryDict : [String: String]) -> Self {
        
        if(((queryDict["mode"]) != nil)){
            // 登録時
            if ((queryDict["mode"]) == "registration"){
                return .SignatureView
            }
            // 変更時
            if ((queryDict["mode"]) == "replacement"){
                return .SignatureView
            }
        }
        
        return .UserVerificationView
    }
}

@main
struct MyNumberCardAuthApp: App {
    @State var urlComponents: URLComponents?
    @State var queryDict : [String: String]?
    @State var authenticationController:AuthenticationController = AuthenticationController()
    
    var body: some Scene {
        WindowGroup {
            ContentView(authenticationController: authenticationController,controller: self.authenticationController.controllerForUserVerification,controllerForSignature: self.authenticationController.controllerForSignature).onOpenURL(perform: { url in
                self.authenticationController.onOpenURL(url: url)
            })
        }
    }
}

func generateQueryDictionary(from components: URLComponents) -> [String: String] {
    var queryDictionary = [String: String]()
    if let queryItems = components.queryItems {
        for queryItem in queryItems {
            if let value = queryItem.value {
                queryDictionary[queryItem.name] = value
            }
        }
    }
    return queryDictionary
}
