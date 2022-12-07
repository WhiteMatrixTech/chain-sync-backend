package com.chainsync.etl.controller;

import com.chainsync.etl.model.response.QueryBlockResponse;
import com.chainsync.etl.model.response.QueryEventsResponse;
import com.chainsync.etl.model.response.QueryHandlerResponse;
import com.chainsync.etl.model.response.QueryTaskLogResponse;
import com.chainsync.etl.model.response.QueryTaskResponse;
import com.chainsync.etl.model.response.QueryTransactionResponse;
import com.chainsync.etl.service.BlockchainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author richard
 */
@Log4j2
@Api(tags = "query api")
@RestController
@RequestMapping("/v1/blockchain")
public class BlockchainController {

  @Resource BlockchainService blockchainService;

  @ApiOperation(value = "task data")
  @GetMapping("/tasks")
  public QueryTaskResponse queryTask() {
    return this.blockchainService.queryTask();
  }

  @ApiOperation(value = "task log data")
  @GetMapping("/taskLogs")
  public QueryTaskLogResponse queryTaskRunning(@RequestParam final String taskName) {
    return this.blockchainService.queryTaskName(taskName);
  }

  @ApiOperation(value = "event handler data")
  @GetMapping("/handlers")
  public QueryHandlerResponse queryHandler() {
    return this.blockchainService.queryHandler();
  }

  @ApiOperation(value = "block data")
  @GetMapping("/blocks")
  public QueryBlockResponse queryBlock(
      @RequestParam final String chainType,
      @RequestParam(required = false) final Long blockNumber) {
    return this.blockchainService.queryBlock(chainType, blockNumber);
  }

  @ApiOperation(value = "transaction data")
  @GetMapping("/transactions")
  public QueryTransactionResponse queryTransaction(
      @RequestParam final String chainType,
      @RequestParam(required = false) final Long blockNumber) {
    return this.blockchainService.queryTransaction(chainType, blockNumber);
  }

  @ApiOperation(value = "events data")
  @GetMapping("/events")
  public QueryEventsResponse queryEvents(@RequestParam final String chainType) {
    return this.blockchainService.queryEvents(chainType);
  }
}
