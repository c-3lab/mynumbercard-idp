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
   }

   private RequstInfo requstInfo;
   
   PlatformRequestModel(String sender) {
       requstInfo = new RequstInfo(sender);
   }

   public JSONObject toJsonObject() {
       JSONObject jsonObj = new JSONObject();
       jsonObj.put(Filed.REQUEST_INFO.name(), requstInfo.toJsonObject());
       jsonObj.put(certificateType.name(), certificate);
       jsonObj.put(Filed.APPLICANT_DATA.name(), applicantData);
       jsonObj.put(Filed.SIGN.name(), sign);
       return jsonObj;
   }

   public static class RequstInfo {
       protected static enum Filed {
           TRANSACTION_ID("transactionId"),
           RECIPIENT("recipient"),
           SENDER("sender"),
           TIME_STAMP("ts");

           private String name;

           private Filed(String formName) {
               name = formName;
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

       RequstInfo(String sender) {
           this.sender = sender;
       }

       public String getTransactionId() {
           return transactionId;
       }

       public RequstInfo setTransactionId(String transactionId) {
           this.transactionId = transactionId;
           return this;
       }

       public RequstInfo setTransactionId(UUID transactionId) {
           this.transactionId = transactionId.toString();
           return this;
       }

       public String getRecipient() {
           return recipient;
       }

       public RequstInfo setRecipient(String recipient) {
           this.recipient = recipient;
           return this;
       }

       public String getSender() {
           return sender;
       }

       public RequstInfo setSender(String sender) {
           this.sender = sender;
           return this;
       }

       public String getTimeStamp() {
           return timeStamp;
       }

       public RequstInfo setTimeStamp(String timeStamp) {
           this.timeStamp = timeStamp;
           return this;
       }

       public JSONObject toJsonObject() {
           JSONObject jsonObj = new JSONObject();
           jsonObj.put(Filed.TRANSACTION_ID.name(), transactionId);
           jsonObj.put(Filed.RECIPIENT.name(), recipient);
           jsonObj.put(Filed.SENDER.name(), sender);
           jsonObj.put(Filed.TIME_STAMP.name(), timeStamp);
           return jsonObj;
       }
   }
}
