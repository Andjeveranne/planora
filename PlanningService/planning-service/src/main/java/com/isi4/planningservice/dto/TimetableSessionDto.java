package com.isi4.planningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableSessionDto {
    private Long id;
    private Long ueId;
    private String ueCode;
    private String ueLibelle;
    private String teacherId;
    private String teacherName;
    private String roomId;
    private String roomCode;
    private String groupId;
    private String groupCode;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
