buildMvn {
  buildNode = 'jenkins-agent-java17'
  publishModDescriptor = true
  mvnDeploy = true

  doDocker = {
    buildJavaDocker {
      publishMaster = true
      healthChk = false
      healthChkCmd = 'wget --no-verbose --tries=1 --spider http://localhost:8081/admin/health || exit 1'
    }
  }
}
