<template>
  <div class="app-container">
    <el-container>
      <el-header class="header p-5 min-w-10">
        <div class="header-content">
          <h1>Data Integration Platform</h1>
          <p class="subtitle">Seamlessly transfer data between ClickHouse and Flat Files</p>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-card class="data-flow-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="header-title">Data Flow Configuration</span>
                  <el-tag type="info" effect="dark">v1.0</el-tag>
                </div>
              </template>
              
              <el-steps :active="activeStep" finish-status="success" class="steps">
                <el-step title="Select Source" description="Choose your data source"></el-step>
                <el-step title="Configure" description="Set up the transfer"></el-step>
                <el-step title="Execute" description="Run the transfer"></el-step>
              </el-steps>

              <el-form :model="form" label-width="120px" class="data-form">
                <!-- Source Selection Section -->
                <el-divider content-position="center">
                  <el-tag type="primary" size="large">Source Selection</el-tag>
                </el-divider>

                <el-form-item label="Source Type" class="source-type">
                  <el-radio-group v-model="form.sourceType" @change="handleSourceChange">
                    <el-radio-button label="clickhouse">
                      <el-icon><DataLine /></el-icon>
                      ClickHouse
                    </el-radio-button>
                    <el-radio-button label="file">
                      <el-icon><Document /></el-icon>
                      Flat File
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>

                <!-- Connection Configuration Section -->
                <template v-if="form.sourceType === 'clickhouse'">
                  <el-divider content-position="center">
                    <el-tag type="primary" size="large">Connection Configuration</el-tag>
                  </el-divider>

                  <el-form-item label="Connection Type" class="connection-type">
                    <el-radio-group v-model="form.connectionType">
                      <el-radio-button label="http">HTTP</el-radio-button>
                      <el-radio-button label="https">HTTPS</el-radio-button>
                    </el-radio-group>
                  </el-form-item>

                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="Host" required>
                        <el-input v-model="form.host" placeholder="e.g., localhost">
                          <template #prefix>
                            <el-icon><Connection /></el-icon>
                          </template>
                        </el-input>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="Port" required>
                        <el-input v-model="form.port" placeholder="e.g., 8123">
                          <template #prefix>
                            <el-icon><Port /></el-icon>
                          </template>
                        </el-input>
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="Database" required>
                        <el-input v-model="form.database" placeholder="e.g., default">
                          <template #prefix>
                            <el-icon><DataBase /></el-icon>
                          </template>
                        </el-input>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="User" required>
                        <el-input v-model="form.user" placeholder="e.g., default">
                          <template #prefix>
                            <el-icon><User /></el-icon>
                          </template>
                        </el-input>
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-form-item label="JWT Token">
                    <el-input
                      v-model="form.jwtToken"
                      type="password"
                      placeholder="Enter JWT token if required"
                      show-password
                    >
                      <template #prefix>
                        <el-icon><Key /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" @click="testConnection" :loading="testingConnection">
                      <el-icon><Connection /></el-icon>
                      Test Connection
                    </el-button>
                    <el-button type="success" @click="fetchTables" :loading="loadingTables" :disabled="!isConnectionValid">
                      <el-icon><Refresh /></el-icon>
                      Load Tables
                    </el-button>
                  </el-form-item>

                  <!-- Schema Discovery Section -->
                  <el-divider content-position="center" v-if="tables.length > 0">
                    <el-tag type="primary" size="large">Schema Discovery</el-tag>
                  </el-divider>

                  <el-form-item label="Tables" class="tables-list" v-if="tables.length > 0">
                    <el-select
                      v-model="form.selectedTable"
                      placeholder="Select a table"
                      @change="handleTableSelect"
                      :loading="loadingTables"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="table in tables"
                        :key="table.name"
                        :label="table.name"
                        :value="table.name"
                      />
                    </el-select>
                  </el-form-item>

                  <el-form-item label="Columns" v-if="columns.length > 0">
                    <el-card class="columns-card">
                      <template #header>
                        <div class="columns-header">
                          <span>Available Columns</span>
                          <el-button-group>
                            <el-button @click="selectAllColumns(true)" size="small">
                              Select All
                            </el-button>
                            <el-button @click="selectAllColumns(false)" size="small">
                              Deselect All
                            </el-button>
                          </el-button-group>
                        </div>
                      </template>
                      <el-checkbox-group v-model="form.selectedColumns">
                        <el-row :gutter="20">
                          <el-col :span="8" v-for="column in columns" :key="column.name">
                            <el-checkbox :label="column.name">
                              {{ column.name }}
                              <el-tag size="small" type="info" class="column-type">
                                {{ column.type }}
                              </el-tag>
                            </el-checkbox>
                          </el-col>
                        </el-row>
                      </el-checkbox-group>
                    </el-card>
                  </el-form-item>

                  <el-form-item label="Query" class="query-input">
                    <el-input
                      type="textarea"
                      v-model="form.query"
                      :rows="6"
                      placeholder="Enter your SQL query"
                      resize="none"
                    ></el-input>
                    <div class="query-tips">
                      <el-tag size="small" type="info">Tip: Use SELECT * FROM table_name to export all data</el-tag>
                    </div>
                  </el-form-item>
                </template>

                <!-- Flat File Configuration Section -->
                <template v-else>
                  <el-divider content-position="center">
                    <el-tag type="primary" size="large">File Configuration</el-tag>
                  </el-divider>

                  <el-form-item label="File Format" class="file-format">
                    <el-radio-group v-model="form.fileFormat">
                      <el-radio-button label="csv">CSV</el-radio-button>
                      <el-radio-button label="tsv">TSV</el-radio-button>
                      <el-radio-button label="json">JSON</el-radio-button>
                    </el-radio-group>
                  </el-form-item>

                  <template v-if="form.fileFormat !== 'json'">
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="Delimiter" required>
                          <el-input v-model="form.delimiter" placeholder="e.g., , or \t">
                            <template #prefix>
                              <el-icon><Separator /></el-icon>
                            </template>
                          </el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="Quote Character">
                          <el-input v-model="form.quoteChar" placeholder="e.g., &quot;">
                            <template #prefix>
                              <el-icon><Quote /></el-icon>
                            </template>
                          </el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>

                    <el-form-item label="Has Header" class="header-row">
                      <el-switch
                        v-model="form.hasHeader"
                        active-text="Yes"
                        inactive-text="No"
                      />
                    </el-form-item>
                  </template>

                  <el-form-item label="File" class="file-upload">
                    <el-upload
                      class="upload-demo"
                      drag
                      action="#"
                      :auto-upload="false"
                      :on-change="handleFileChange"
                      :limit="1"
                      :accept="getFileAcceptType"
                    >
                      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                      <div class="el-upload__text">
                        Drop file here or <em>click to upload</em>
                      </div>
                      <template #tip>
                        <div class="el-upload__tip">
                          {{ getFileTypeDescription }}
                        </div>
                      </template>
                    </el-upload>
                  </el-form-item>

                  <el-form-item label="Table Name" class="table-name">
                    <el-input 
                      v-model="form.tableName" 
                      placeholder="Enter target table name"
                    >
                      <template #prefix>
                        <el-icon><Grid /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item label="Preview" v-if="form.file">
                    <el-button type="primary" @click="previewFile" :loading="previewLoading">
                      <el-icon><View /></el-icon>
                      Preview Data
                    </el-button>
                  </el-form-item>

                  <el-form-item label="Columns" v-if="previewHeaders.length > 0">
                    <el-card class="columns-card">
                      <template #header>
                        <div class="columns-header">
                          <span>Available Columns</span>
                          <el-button-group>
                            <el-button @click="selectAllColumns(true)" size="small">
                              Select All
                            </el-button>
                            <el-button @click="selectAllColumns(false)" size="small">
                              Deselect All
                            </el-button>
                          </el-button-group>
                        </div>
                      </template>
                      <el-checkbox-group v-model="form.selectedColumns">
                        <el-row :gutter="20">
                          <el-col :span="8" v-for="header in previewHeaders" :key="header">
                            <el-checkbox :label="header">
                              {{ header }}
                            </el-checkbox>
                          </el-col>
                        </el-row>
                      </el-checkbox-group>
                    </el-card>
                  </el-form-item>
                </template>

                <!-- Action Buttons Section -->
                <el-divider content-position="center">
                  <el-tag type="primary" size="large">Actions</el-tag>
                </el-divider>

                <el-form-item class="submit-button">
                  <el-button 
                    type="primary" 
                    @click="handleSubmit"
                    :loading="loading"
                    size="large"
                    :disabled="form.sourceType === 'clickhouse' && !isConnectionValid"
                  >
                    <el-icon><Upload /></el-icon>
                    Start Ingestion
                  </el-button>
                </el-form-item>

                <!-- Progress Dialog -->
                <el-dialog
                  v-model="showProgressDialog"
                  title="Transfer Progress"
                  width="50%"
                  center
                  :close-on-click-modal="false"
                  :close-on-press-escape="false"
                  :show-close="false"
                >
                  <el-progress
                    :percentage="transferProgress"
                    :status="transferStatus"
                    :stroke-width="20"
                  />
                  <div class="progress-details">
                    <p>Records Processed: {{ processedRecords }}</p>
                    <p>Status: {{ transferStatusText }}</p>
                    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
                  </div>
                  <template #footer>
                    <span class="dialog-footer">
                      <el-button 
                        type="primary" 
                        @click="showProgressDialog = false"
                        :disabled="!transferComplete"
                      >
                        Close
                      </el-button>
                    </span>
                  </template>
                </el-dialog>
              </el-form>
            </el-card>
          </el-col>
        </el-row>

        <!-- Success Dialog -->
        <el-dialog
          v-model="showSuccessDialog"
          title="Transfer Successful"
          width="30%"
          center
        >
          <div class="success-details">
            <el-icon class="success-icon"><CircleCheck /></el-icon>
            <p>Your data transfer has been completed successfully!</p>
            <p class="record-count">Total Records Processed: {{ processedRecords }}</p>
          </div>
          <template #footer>
            <span class="dialog-footer">
              <el-button type="primary" @click="showSuccessDialog = false">
                Close
              </el-button>
            </span>
          </template>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { 
  DataLine, 
  Document, 
  Upload, 
  UploadFilled,
  Grid,
  Connection,
  Port,
  DataBase,
  User,
  Key,
  Separator,
  Quote,
  View,
  Refresh,
  CircleCheck
} from '@element-plus/icons-vue'

export default {
  name: 'App',
  components: {
    DataLine,
    Document,
    Upload,
    UploadFilled,
    Grid,
    Connection,
    Port,
    DataBase,
    User,
    Key,
    Separator,
    Quote,
    View,
    Refresh,
    CircleCheck
  },
  setup() {
    const form = ref({
      sourceType: 'clickhouse',
      connectionType: 'http',
      host: '',
      port: '',
      database: '',
      user: '',
      jwtToken: '',
      query: '',
      tableName: '',
      file: null,
      fileFormat: 'csv',
      delimiter: ',',
      quoteChar: '"',
      hasHeader: true,
      selectedTable: '',
      selectedColumns: []
    })

    const activeStep = ref(0)
    const loading = ref(false)
    const testingConnection = ref(false)
    const showSuccessDialog = ref(false)
    const showPreviewDialog = ref(false)
    const previewLoading = ref(false)
    const previewData = ref([])
    const previewHeaders = ref([])
    const tables = ref([])
    const columns = ref([])
    const loadingTables = ref(false)

    const showProgressDialog = ref(false)
    const transferProgress = ref(0)
    const transferStatus = ref('')
    const transferStatusText = ref('')
    const processedRecords = ref(0)
    const errorMessage = ref('')
    const transferComplete = ref(false)

    const isConnectionValid = computed(() => {
      return form.value.host && 
             form.value.port && 
             form.value.database && 
             form.value.user
    })

    const getFileAcceptType = computed(() => {
      switch (form.value.fileFormat) {
        case 'csv':
          return '.csv'
        case 'tsv':
          return '.tsv'
        case 'json':
          return '.json'
        default:
          return '*'
      }
    })

    const getFileTypeDescription = computed(() => {
      switch (form.value.fileFormat) {
        case 'csv':
          return 'CSV files up to 10MB'
        case 'tsv':
          return 'TSV files up to 10MB'
        case 'json':
          return 'JSON files up to 10MB'
        default:
          return 'Files up to 10MB'
      }
    })

    const handleSourceChange = () => {
      activeStep.value = 1
    }

    const handleFileChange = (file) => {
      form.value.file = file.raw
      activeStep.value = 1
    }

    const testConnection = async () => {
      testingConnection.value = true
      try {
        const { data } = await axios.post('/api/data-flow/test-connection', {
          connectionType: form.value.connectionType,
          host: form.value.host,
          port: form.value.port,
          database: form.value.database,
          user: form.value.user,
          jwtToken: form.value.jwtToken
        })
        ElMessage.success(data.message || 'Connection successful!')
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Connection failed')
      } finally {
        testingConnection.value = false
      }
    }

    const fetchTables = async () => {
      loadingTables.value = true
      try {
        const { data } = await axios.post('/api/data-flow/tables', {
          connectionType: form.value.connectionType,
          host: form.value.host,
          port: form.value.port,
          database: form.value.database,
          user: form.value.user,
          jwtToken: form.value.jwtToken
        })
        tables.value = data.tables
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Failed to fetch tables')
      } finally {
        loadingTables.value = false
      }
    }

    const handleTableSelect = async (tableName) => {
      if (!tableName) return
      
      try {
        const { data } = await axios.post('/api/data-flow/columns', {
          connectionType: form.value.connectionType,
          host: form.value.host,
          port: form.value.port,
          database: form.value.database,
          user: form.value.user,
          jwtToken: form.value.jwtToken,
          table: tableName
        })
        columns.value = data.columns
        form.value.selectedColumns = columns.value.map(col => col.name)
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Failed to fetch columns')
      }
    }

    const selectAllColumns = (select) => {
      if (form.value.sourceType === 'clickhouse') {
        form.value.selectedColumns = select ? columns.value.map(col => col.name) : []
      } else {
        form.value.selectedColumns = select ? previewHeaders.value : []
      }
    }

    const previewFile = async () => {
      previewLoading.value = true
      try {
        const formData = new FormData()
        formData.append('file', form.value.file)
        formData.append('format', form.value.fileFormat)
        formData.append('delimiter', form.value.delimiter)
        formData.append('quoteChar', form.value.quoteChar)
        formData.append('hasHeader', form.value.hasHeader)

        const { data } = await axios.post('/api/data-flow/preview-file', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        previewData.value = data.rows
        previewHeaders.value = data.headers
        form.value.selectedColumns = data.headers
        showPreviewDialog.value = true
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Failed to preview file')
      } finally {
        previewLoading.value = false
      }
    }

    const handleSubmit = async () => {
      loading.value = true
      showProgressDialog.value = true
      transferProgress.value = 0
      transferStatus.value = ''
      transferStatusText.value = 'Initializing transfer...'
      processedRecords.value = 0
      errorMessage.value = ''
      transferComplete.value = false

      try {
        if (form.value.sourceType === 'clickhouse') {
          const response = await axios.post('/api/data-flow/clickhouse-to-file', {
            connectionType: form.value.connectionType,
            host: form.value.host,
            port: form.value.port,
            database: form.value.database,
            user: form.value.user,
            jwtToken: form.value.jwtToken,
            query: form.value.query,
            selectedColumns: form.value.selectedColumns
          }, {
            onUploadProgress: (progressEvent) => {
              const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
              transferProgress.value = progress
              transferStatusText.value = `Transferring data... (${progress}%)`
            }
          })
          
          processedRecords.value = response.data.recordsProcessed
          transferProgress.value = 100
          transferStatus.value = 'success'
          transferStatusText.value = 'Transfer completed successfully'
          transferComplete.value = true
          showSuccessDialog.value = true
        } else {
          const formData = new FormData()
          formData.append('file', form.value.file)
          formData.append('tableName', form.value.tableName)
          formData.append('format', form.value.fileFormat)
          formData.append('delimiter', form.value.delimiter)
          formData.append('quoteChar', form.value.quoteChar)
          formData.append('hasHeader', form.value.hasHeader)
          formData.append('selectedColumns', JSON.stringify(form.value.selectedColumns))
          
          const response = await axios.post('/api/data-flow/file-to-clickhouse', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: (progressEvent) => {
              const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
              transferProgress.value = progress
              transferStatusText.value = `Uploading file... (${progress}%)`
            }
          })

          processedRecords.value = response.data.recordsProcessed
          transferProgress.value = 100
          transferStatus.value = 'success'
          transferStatusText.value = 'Transfer completed successfully'
          transferComplete.value = true
          showSuccessDialog.value = true
        }
      } catch (error) {
        transferProgress.value = 100
        transferStatus.value = 'exception'
        transferComplete.value = true
        
        if (error.response) {
          switch (error.response.status) {
            case 401:
              errorMessage.value = 'Authentication failed. Please check your credentials.'
              break
            case 403:
              errorMessage.value = 'Access denied. Please check your permissions.'
              break
            case 404:
              errorMessage.value = 'Resource not found. Please check your configuration.'
              break
            case 500:
              errorMessage.value = 'Server error. Please try again later.'
              break
            default:
              errorMessage.value = error.response.data?.message || 'An error occurred during transfer'
          }
        } else if (error.request) {
          errorMessage.value = 'Network error. Please check your connection.'
        } else {
          errorMessage.value = error.message || 'An unexpected error occurred'
        }
        
        transferStatusText.value = 'Transfer failed'
        ElMessage.error(errorMessage.value)
      } finally {
        loading.value = false
      }
    }

    return {
      form,
      activeStep,
      loading,
      testingConnection,
      showSuccessDialog,
      showPreviewDialog,
      previewLoading,
      previewData,
      previewHeaders,
      isConnectionValid,
      getFileAcceptType,
      getFileTypeDescription,
      handleSourceChange,
      handleFileChange,
      testConnection,
      previewFile,
      handleSubmit,
      tables,
      columns,
      loadingTables,
      fetchTables,
      handleTableSelect,
      selectAllColumns,
      showProgressDialog,
      transferProgress,
      transferStatus,
      transferStatusText,
      processedRecords,
      errorMessage,
      transferComplete
    }
  }
}
</script>

<style>
.app-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  position: relative;
  overflow: hidden;
}

.app-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    linear-gradient(45deg, rgba(64, 158, 255, 0.1) 25%, transparent 25%, transparent 75%, rgba(64, 158, 255, 0.1) 75%),
    linear-gradient(45deg, rgba(64, 158, 255, 0.1) 25%, transparent 25%, transparent 75%, rgba(64, 158, 255, 0.1) 75%);
  background-size: 60px 60px;
  background-position: 0 0, 30px 30px;
  animation: backgroundMove 20s linear infinite;
  z-index: 0;
}

@keyframes backgroundMove {
  0% {
    background-position: 0 0, 30px 30px;
  }
  100% {
    background-position: 60px 60px, 90px 90px;
  }
}

.app-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, transparent 0%, rgba(255, 255, 255, 0.8) 100%);
  z-index: 1;
}

.el-container {
  position: relative;
  z-index: 2;
}

.header {
  background: linear-gradient(135deg, #409EFF 0%, #36D1DC 100%);
  color: white;
  padding: 20px 0;
  text-align: center;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
}

.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    linear-gradient(45deg, rgba(255,255,255,0.1) 25%, transparent 25%, transparent 50%, rgba(255,255,255,0.1) 50%, rgba(255,255,255,0.1) 75%, transparent 75%, transparent),
    linear-gradient(45deg, rgba(255,255,255,0.1) 25%, transparent 25%, transparent 50%, rgba(255,255,255,0.1) 50%, rgba(255,255,255,0.1) 75%, transparent 75%, transparent);
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;
  animation: move 2s linear infinite;
}

@keyframes move {
  0% { background-position: 0 0, 10px 10px; }
  100% { background-position: 20px 20px, 30px 30px; }
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.header h1 {
  margin: 0;
  font-size: 2.5em;
  font-weight: 600;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
  animation: fadeInDown 1s ease-out;
}

.subtitle {
  margin: 10px 0 0;
  font-size: 1.1em;
  opacity: 0.9;
  animation: fadeInUp 1s ease-out 0.3s both;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 0.9;
    transform: translateY(0);
  }
}

.main-content {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
  animation: fadeIn 1s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.data-flow-card {
  border-radius: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  background: white;
  overflow: hidden;
}

.data-flow-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(to right, #f8f9fa, #ffffff);
  border-bottom: 1px solid #ebeef5;
}

.header-title {
  font-size: 1.2em;
  font-weight: 600;
  color: #303133;
}

.steps {
  margin: 30px 0;
  padding: 0 20px;
}

.el-step__title {
  font-weight: 600;
  transition: all 0.3s;
}

.el-step__title:hover {
  color: #409EFF;
  transform: scale(1.05);
}

.data-form {
  margin-top: 20px;
  padding: 20px;
}

.source-type, .connection-type {
  margin-bottom: 30px;
  transition: all 0.3s;
}

.el-radio-button__inner {
  transition: all 0.3s;
}

.el-radio-button__inner:hover {
  transform: scale(1.05);
}

.query-input {
  margin-bottom: 20px;
}

.query-tips {
  margin-top: 10px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.file-format {
  margin-bottom: 20px;
}

.header-row {
  margin-bottom: 20px;
}

.file-upload {
  margin-bottom: 20px;
}

.upload-demo {
  width: 100%;
  transition: all 0.3s;
}

.upload-demo:hover {
  transform: scale(1.02);
}

.table-name {
  margin-bottom: 30px;
}

.submit-button {
  text-align: center;
  margin-top: 40px;
}

.el-button {
  padding: 12px 24px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.el-button::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 5px;
  height: 5px;
  background: rgba(255,255,255,0.5);
  opacity: 0;
  border-radius: 100%;
  transform: scale(1, 1) translate(-50%);
  transform-origin: 50% 50%;
}

.el-button:focus:not(:active)::after {
  animation: ripple 1s ease-out;
}

@keyframes ripple {
  0% {
    transform: scale(0, 0);
    opacity: 0.5;
  }
  100% {
    transform: scale(20, 20);
    opacity: 0;
  }
}

.el-icon {
  margin-right: 5px;
  transition: transform 0.3s;
}

.el-button:hover .el-icon {
  transform: scale(1.2);
}

.dialog-footer {
  display: flex;
  justify-content: center;
}

.el-row {
  margin-bottom: 20px;
}

.preview-table {
  margin-top: 20px;
}

.tables-list {
  display: flex;
  gap: 10px;
  align-items: center;
}

.refresh-tables {
  margin-left: 10px;
}

.columns-card {
  margin-top: 10px;
  transition: all 0.3s;
}

.columns-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.columns-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.column-type {
  margin-left: 5px;
  transition: all 0.3s;
}

.column-type:hover {
  transform: scale(1.1);
}

.el-checkbox {
  margin-bottom: 10px;
  transition: all 0.3s;
}

.el-checkbox:hover {
  transform: translateX(5px);
}

.progress-details {
  margin-top: 20px;
  text-align: center;
  animation: fadeIn 0.5s ease-out;
}

.error-message {
  color: #F56C6C;
  margin-top: 10px;
  animation: shake 0.5s;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.success-details {
  text-align: center;
  animation: zoomIn 0.5s ease-out;
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.success-icon {
  font-size: 48px;
  color: #67C23A;
  margin-bottom: 20px;
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.record-count {
  font-weight: bold;
  margin-top: 10px;
  color: #409EFF;
  animation: pulse 2s infinite;
}

/* Add smooth transitions for form elements */
.el-input, .el-select, .el-textarea {
  transition: all 0.3s;
}

.el-input:hover, .el-select:hover, .el-textarea:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* Add loading animation */
.el-loading-spinner {
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Add hover effects for cards */
.el-card {
  transition: all 0.3s;
}

.el-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

/* Add animation for progress bar */
.el-progress-bar__inner {
  transition: width 0.6s ease;
}

/* Add animation for dialogs */
.el-dialog {
  animation: dialogFadeIn 0.3s ease-out;
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style> 