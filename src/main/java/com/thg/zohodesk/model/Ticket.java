package com.thg.zohodesk.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Ticket {
    private String id;
    private String subject;
    private String description;
    private String status;
    private String departmentId;
    private String contactId;
    private String priority;
    private String category;
    private String subCategory;
    private String createdTime;
    private String modifiedTime;
}