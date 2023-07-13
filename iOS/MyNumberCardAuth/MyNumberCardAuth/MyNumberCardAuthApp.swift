//
//  MyNumberCardAuthApp.swift
//  MyNumberCardAuth
//
//  Created by abelstaff on 2023/04/20.
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
    @State var controller = ViewController()
    @State var controllerForSignature = SignatureViewController()
    var body: some Scene {
        WindowGroup {
            ContentView(urlComponents: $urlComponents, queryDict: $queryDict,authenticationController: authenticationController,controller: controller,controllerForSignature: controllerForSignature).onOpenURL(perform: { url in
                
                let urlComponents = URLComponents(url: url, resolvingAgainstBaseURL: true)
                self.urlComponents = urlComponents
                if let urlComponents = urlComponents {
                    self.queryDict = generateQueryDictionary(from: urlComponents)
                    if let query = queryDict {
                        self.authenticationController.viewState = authenticationController.viewState.isViewMode(queryDict: query)
                        self.authenticationController.runMode = authenticationController.runMode.isMode(queryDict: query)
                        self.authenticationController.queryDict = query;
                        self.authenticationController.clear()
                        self.authenticationController.setOpenURL(queryDict: query)
                        
                        if let actionURL = query["action_url"], let nonse = query["nonce"]
                        {
                            self.controller.inputPIN = ""
                            self.controllerForSignature.inputPIN = ""
                            self.controller.actionURL = actionURL
                            self.controllerForSignature.actionURL = actionURL
                            self.controller.nonce = nonse
                            self.controllerForSignature.nonce = nonse
                        }
                    }
                }
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
