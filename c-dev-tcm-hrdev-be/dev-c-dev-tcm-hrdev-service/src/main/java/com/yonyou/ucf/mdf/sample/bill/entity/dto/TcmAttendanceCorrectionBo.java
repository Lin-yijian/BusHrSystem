package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TcmAttendanceCorrectionBo {

    private String type;

    private List<String> ids;

    private String id;

    private String employeeId;

    private String date;

}
