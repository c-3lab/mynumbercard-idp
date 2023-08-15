package com.example.mynumbercardidp.keycloak.network.platform;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * プラットフォームAPIへ送信するリクエスト内容の構造体を表すクラスです。
 */
public class PlatformRequestModel extends CommonRequestModel {
   public static enum Filed {
       REQUEST_INFO("requestInfo"),
       APPLICANT_DATA("applicantData"),
       SIGN("sign");

       private String name;

       private Filed(String formName) {
           name = formName;
       }

       public String getName() {
           return name;
       }
   }

   private RequestInfo requestInfo;
   
   protected PlatformRequestModel() {
       requestInfo = new RequestInfo();
   }

   protected PlatformRequestModel(String sender) {
       requestInfo = new RequestInfo(sender);
   }

   public RequestInfo getRequestInfo() {
       return requestInfo;
   }

   protected PlatformRequestModel setRequestInfo(RequestInfo requestInfo) {
       this.requestInfo = requestInfo;
       return this;
   }

   protected JSONObject toJsonObject() {
       JSONObject jsonObj = new JSONObject();
       jsonObj.put(Filed.REQUEST_INFO.getName(), requestInfo.toJsonObject());
       jsonObj.put(certificateType.getName(), certificate);
       jsonObj.put(Filed.APPLICANT_DATA.getName(), applicantData);
       jsonObj.put(Filed.SIGN.getName(), sign);
       return jsonObj;
   }

   public static class RequestInfo {
       public static enum Filed {
           TRANSACTION_ID("transactionId"),
           RECIPIENT("recipient"),
           SENDER("sender"),
           TIME_STAMP("ts");

           private String name;

           private Filed(String formName) {
               name = formName;
           }

           public String getName() {
               return name;
           }
       }

       private String transactionId;
       private String recipient;
       private String sender;
       private String timeStamp;

       {
           transactionId = UUID.randomUUID().toString();
           recipient = "JPKI";
           timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss.SSS").toString();
       }

       public RequestInfo() {}

       protected RequestInfo(String sender) {
           this.sender = sender;
       }

       public String getTransactionId() {
           return transactionId;
       }

       protected RequestInfo setTransactionId(String transactionId) {
           this.transactionId = transactionId;
           return this;
       }

       protected RequestInfo setTransactionId(UUID transactionId) {
           this.transactionId = transactionId.toString();
           return this;
       }

       public String getRecipient() {
           return recipient;
       }

       protected RequestInfo setRecipient(String recipient) {
           this.recipient = recipient;
           return this;
       }

       public String getSender() {
           return sender;
       }

       protected RequestInfo setSender(String sender) {
           this.sender = sender;
           return this;
       }

       public String getTimeStamp() {
           return timeStamp;
       }

       protected RequestInfo setTimeStamp(String timeStamp) {
           this.timeStamp = timeStamp;
           return this;
       }

       protected JSONObject toJsonObject() {
           JSONObject jsonObj = new JSONObject();
           jsonObj.put(Filed.TRANSACTION_ID.getName(), transactionId);
           jsonObj.put(Filed.RECIPIENT.getName(), recipient);
           jsonObj.put(Filed.SENDER.getName(), sender);
           jsonObj.put(Filed.TIME_STAMP.getName(), timeStamp);
           return jsonObj;
       }
   }
}
