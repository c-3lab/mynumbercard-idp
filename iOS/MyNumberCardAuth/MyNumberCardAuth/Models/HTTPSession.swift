//
//  HTTPSession.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/04/20.
//

import Foundation
import UIKit

public class HTTPSession: NSObject, URLSessionDelegate, URLSessionTaskDelegate{
    private var authenticationController:AuthenticationController
    
    init(authenticationController: AuthenticationController) {
        self.authenticationController = authenticationController
    }
    
    public func urlSession(_ session: URLSession, task: URLSessionTask, willPerformHTTPRedirection response: HTTPURLResponse, newRequest request: URLRequest, completionHandler: @escaping (URLRequest?) -> Void) {
        completionHandler(nil)
    }
    
    private func getDataFromServerWithSuccess(request : URLRequest, noRedirect: Bool, success: @escaping (_ response: URLResponse?) -> Void){
        var HTTPSessionDelegate: HTTPSession? = nil
        if noRedirect {
            HTTPSessionDelegate = HTTPSession(authenticationController: self.authenticationController)
        }
        let session = URLSession(configuration: URLSessionConfiguration.default, delegate: HTTPSessionDelegate, delegateQueue: nil)
        let task = session.dataTask(with: request){ (data, response, error) in
            if let error = error {
                print(error)
            }
            success(response)
        }
        task.resume()
    }
    
    public func openRedirectURLOnSafari(request: URLRequest){
        getDataFromServerWithSuccess(request : request, noRedirect: true){(response) -> Void in
            
            if let httpResponse = response as? HTTPURLResponse
            {
                print("API result")
                print(httpResponse.statusCode)
                print(httpResponse.allHeaderFields)
                
                // ステータスコード判定
                if(httpResponse.statusCode == 400 ||
                   httpResponse.statusCode == 500 ||
                   httpResponse.statusCode == 503){
                    DispatchQueue.main.async {
                        self.authenticationController.isAlert = true
                        self.authenticationController.messageTitle = NSLocalizedString("failure", comment: "失敗")
                        self.authenticationController.messageString = NSLocalizedString("unexpected", comment: "予期せぬエラーが発生しました。")
                    }
                }
                
                if(httpResponse.statusCode == 404){
                    DispatchQueue.main.async {
                        self.authenticationController.isAlert = true
                        self.authenticationController.isErrorOpenURL = true
                        self.authenticationController.messageTitle = NSLocalizedString("authentication failure", comment: "認証失敗")
                        self.authenticationController.messageString = NSLocalizedString("no user", comment: "ユーザー未登録のため、利用者登録を行ってください。")
                    }
                }

                if(httpResponse.statusCode == 401){
                    DispatchQueue.main.async {
                        self.authenticationController.isLinkAlert = true
                        self.authenticationController.messageTitle = NSLocalizedString("authentication failure", comment: "認証失敗")
                        if(self.authenticationController.runMode == .Login){
                            self.authenticationController.messageString = NSLocalizedString("certificate revoked", comment: "利用者証明用電子証明書が失効しています。") + NSLocalizedString("link below", comment: "お住まいの市区町村の窓口へお問い合わせください。")
                        }else{
                            self.authenticationController.messageString = NSLocalizedString("certificate revoked for signature", comment: "署名用電子証明書が失効しています。") + NSLocalizedString("link below", comment: "お住まいの市区町村の窓口へお問い合わせください。")
                        }
                    }
                }
                
                if(httpResponse.statusCode == 409 )
                {
                    DispatchQueue.main.async {
                        self.authenticationController.isAlert = true
                        self.authenticationController.isErrorOpenURL = true
                        self.authenticationController.messageTitle = NSLocalizedString("registration failed", comment: "登録失敗")
                        self.authenticationController.messageString = NSLocalizedString("already registered", comment: "既にユーザーが登録されているため、ログインを行ってください。")
                    }
                }
                
                if(httpResponse.statusCode == 410 )
                {
                    DispatchQueue.main.async {
                        self.authenticationController.viewState = .SignatureView
                        self.authenticationController.runMode = .Replacement
                        self.authenticationController.isAlert = true
                        self.authenticationController.messageTitle = NSLocalizedString("reloading", comment: "マイナンバーカードの再読み込み")
                        self.authenticationController.messageString = NSLocalizedString("change my registration", comment: "登録情報の変更が必要です。変更を行うため署名用電子証明書の読み取りを行ってください。")
                    }
                }
                
                if let redirectURL = httpResponse.allHeaderFields["Location"] as? String,
                   let newURL = URL(string: redirectURL) {
                    print("リダイレクト先のURL: \(newURL)")
                    DispatchQueue.main.async {
                        UIApplication.shared.open(newURL)
                    }
                }
            }
        }
    }
}
