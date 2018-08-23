package com.psagroup.calibrationparserapi.upload;

import java.util.List;
import io.swagger.annotations.ApiModelProperty;

public class ConfigInfo {

  @ApiModelProperty(position = 0, required = true, example = "E494847")
  private String psaID;
  @ApiModelProperty(position = 1, required = true, example = "1")
  private long configId;
  @ApiModelProperty(position = 2, required = true)
  private String filesPath;
  @ApiModelProperty(position = 3, required = true, allowableValues = "EngM_arEffTVA_T,ZFC_phiSetHpRail0_GMAP,WaTEst_volfWaCoPmp_M")
  private List<String> labels;


  
  

  public ConfigInfo() {
    super();
  }



  public ConfigInfo(String psaID, long configId, String filesPath, List<String> labels) {
    super();
    this.psaID = psaID;
    this.configId = configId;
    this.filesPath = filesPath;
    this.labels = labels;
  }



  public boolean isValid() {
    return configId >= 0 && !filesPath.isEmpty();
  }



  public String getPsaID() {
    return psaID;
  }



  public void setPsaID(String psaID) {
    this.psaID = psaID;
  }



  public long getConfigId() {
    return configId;
  }



  public void setConfigId(long configId) {
    this.configId = configId;
  }



  public String getFilesPath() {
    return filesPath;
  }



  public void setFilesPath(String filesPath) {
    this.filesPath = filesPath;
  }



  public List<String> getLabels() {
    return labels;
  }



  public void setLabels(List<String> labels) {
    this.labels = labels;
  }



}
