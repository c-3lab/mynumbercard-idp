//
//  HTTPSession.swift
//  MyNumberCardAuth
//
//  Created by abelstaff on 2023/04/20.
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
                        self.authenticationController.messageTitle = NSLocalizedString("e01-001", comment: "失敗")
                        self.authenticationController.messageString = NSLocalizedString("e01-002", comment: "予期せぬエラーが発生しました。")
                    }
                }
                
                if(httpResponse.statusCode == 404){
                    DispatchQueue.main.async {
                        self.authenticationController.isAlert = true
                        self.authenticationController.isErrorOpenURL = true
                        self.authenticationController.messageTitle = NSLocalizedString("e02-001", comment: "認証失敗")
                        self.authenticationController.messageString = NSLocalizedString("e02-002", comment: "ユーザー未登録のため、利用者登録を行ってください。")
                    }
                }

                if(httpResponse.statusCode == 401){
                    DispatchQueue.main.async {
                        self.authenticationController.isLinkAlert = true
                        self.authenticationController.messageTitle = NSLocalizedString("e03-001", comment: "認証失敗")
                        if(self.authenticationController.runMode == .Login){
                            self.authenticationController.messageString = NSLocalizedString("e03-002", comment: "利用者証明用電子証明書が失効しています。") + NSLocalizedString("e03-004", comment: "お住まいの市区町村の窓口へお問い合わせください。")
                        }else{
                            self.authenticationController.messageString = NSLocalizedString("e03-003", comment: "署名用電子証明書が失効しています。") + NSLocalizedString("e03-004", comment: "お住まいの市区町村の窓口へお問い合わせください。")
                        }
                    }
                }
                
                if(httpResponse.statusCode == 409 )
                {
                    DispatchQueue.main.async {
                        self.authenticationController.isAlert = true
                        self.authenticationController.isErrorOpenURL = true
                        self.authenticationController.messageTitle = NSLocalizedString("e04-001", comment: "登録失敗")
                        self.authenticationController.messageString = NSLocalizedString("e04-002", comment: "既にユーザーが登録されているため、ログインを行ってください。")
                    }
                }
                
                if(httpResponse.statusCode == 410 )
                {
                    DispatchQueue.main.async {
                        
                        self.authenticationController.viewState = .SignatureView
                        self.authenticationController.runMode = .Replacement
                        
                        self.authenticationController.isAlert = true
                        self.authenticationController.messageTitle = NSLocalizedString("e05-001", comment: "マイナンバーカードの再読み込み")
                        self.authenticationController.messageString = NSLocalizedString("e05-002", comment: "登録情報の変更が必要です。変更を行うため署名用電子証明書の読み取りを行ってください。")
                    }
                }
                
                // EC2版(認証APIが製造された場合は不要になる、デモ操作用の処理)
                if let redirectURL = httpResponse.allHeaderFields["Location"] as? String,
                   let newURL = URL(string: redirectURL) {
                    print("リダイレクト先のURL: \(newURL)")
                    DispatchQueue.main.async {
                        UIApplication.shared.open(newURL)
                    }
                }
                
                // ブラウザでリダイレクトを表示
                if let redirectURL = httpResponse.allHeaderFields["redirect_uri"] as? String,
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
