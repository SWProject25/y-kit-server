package com.twojz.y_kit.external.policy.dto;

import java.util.List;
import lombok.Data;

@Data
public class YouthPolicyResponse {
   private int resultCode;
   private String resultMsg;
   private Result result;

   @Data
   public static class Result {
      private Pagging pagging;
      private List<YouthPolicy> youthPolicyList;
   }

   @Data
   public static class Pagging {
      private int totCount;
      private int pageNum;
      private int pageSize;
   }
}