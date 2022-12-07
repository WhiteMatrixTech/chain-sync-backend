package com.matrix.task.controller;

import com.matrix.common.response.Response;
import com.matrix.task.model.TaskDef;
import com.matrix.task.service.TaskDefService;
import com.matrix.task.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api
@Log4j2
@RestController
@RequestMapping(path = "/api/v1/")
public class TaskController {

  @Resource
  private TaskDefService taskDefService;

  @ApiOperation(value = "create or update task def")
  @PostMapping("/def")
  public Response<String> upsertTaskDef(@RequestBody final TaskDef taskDef) {
    return Response.success(taskDefService.upsertTaskDef(taskDef));
  }

  @ApiOperation(value = "delete a task def")
  @DeleteMapping("/def/{taskName}")
  public Response<String> deleteTaskDef(@PathVariable final String taskName) {
    return Response.success(taskDefService.deleteTaskDef(taskName));
  }
}
