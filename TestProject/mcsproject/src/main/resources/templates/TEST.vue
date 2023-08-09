<template>
  <div>
    <el-row class="return-container">
      <h4>Order overview</h4>
      <el-col :span="12" v-show="!carInfo.vehicleId==0">
        <el-card :body-style="{ padding: '0px' }">
          <img v-show="carInfo.vehicleType == 1" src="../../assets/img/bike.jpeg" style="width: 400px">
          <img v-show="carInfo.vehicleType == 2" src="../../assets/img/scooter.jpg" style="width: 400px">
          <!--          <img :src="carInfo.vehicleType == 1 ? require('../../assets/img/bike.jpeg') : require('../../assets/img/scooter.jpg')" style="width: 400px"></img>-->
          <div style="padding: 4px;">
          </div>
        </el-card>
      </el-col>
      <el-col :span="12" style="padding-left: 20px" v-show="!carInfo.vehicleId==0">
        <el-card :body-style="{ padding: '0px' }">
          <div style="padding: 14px; color: #6f7180">
            <h4>Vehicle Id: {{carInfo.vehicleId}}</h4>
            <h4 v-show="carInfo.vehicleType == 1">Type: BIKE</h4>
            <h4 v-show="carInfo.vehicleType == 2">Type: SCOOTER</h4>
            <h4>Start location {{carInfo.slocationName}}</h4>
            <h4>Battery: {{carInfo.battery}}</h4>
            <h4>Start time: {{carInfo.startTime}}</h4>
            <div>
              <h4><time class="time">current time: {{dateFormat(currentDate)}}</time></h4>
            </div>
            <h4>Price: 10￡/h</h4>
            <div style="padding-top: 20px; padding-bottom: 10px; float:right">
              <el-button type="primary" class="button" @click="returnTheCar()">return</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      <h4 style="font-size: medium; color: #3a8ee6" v-show="carInfo.vehicleId==0">NO VEHICLE RENTING NOW</h4>
    </el-row>

    <el-dialog :title="title" :visible.sync="diaFormVisible" width="30%" @click='closeDialog("edit")'>
      <el-form label-width="120px" :data="returnForm" :model="returnForm">
        <el-input placeholder="Please enter end location" v-model="returnForm.eLocationId"></el-input>
        <el-form-item label="You need to pay: " prop="fee">
          <!--          {{returnForm.fee}}-->
          20 ￡
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click='closeDialog("edit")'>cancel</el-button>
        <!--        <el-button size="small" type="primary" :loading="loading" class="title" @click="submitForm('diaForm')">pay</el-button>-->
        <el-button size="small" type="primary" :loading="loading" class="title" @click="submitForm()">pay</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {getOneVehicle, returnBike} from "../../api/userMG";
import Pagination from '../../components/Pagination'
export default {
  data() {
    return {
      userInfo: [],
      carInfo:{
        vehicleId: 0,
        startTime: Date,
        battery: 0,
        slocationId: 0,
        slocationName: " ",
        vehicleType: 0,
      },
      currentDate: new Date(),
      loading: false, //是显示加载
      diaFormVisible: false, //控制编辑页面显示与隐藏
      title: 'Pay',
      // payMoney: 0,
      returnForm:{
        userId: 0,
        fee: 0,
        vehicleId: 0,
        eLocationId : '',
        batteryTest: 0
      },
      diaForm: {
        id: '',
        name: '',
        payType: 1,
      },
      userparm: [], //搜索权限
      listData: [], //用户数据
      pageparm: {
        name: 1,
        currentPage: 1,
        pageSize: 10,
        total: 10
      }
    }
  },

  components: {
    Pagination
  },

  created() {
    this.userInfo = JSON.parse(localStorage.getItem("userdata"))
    this.carInfo.vehicleId = JSON.parse(localStorage.getItem("userdata")).vehicleId
    if (!this.carInfo.vehicleId == 0){
      this.getVehicle()
    }
    console.log(this.userInfo)
  },

  mounted() {
    //显示当前日期时间
    let _this = this// 声明一个变量指向Vue实例this，保证作用域一致
    this.timer = setInterval(() => {
      _this.date = new Date(); // 修改数据date
    }, 1000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer); // 在Vue实例销毁前，清除我们的定时器
    }
  },

  methods: {
    getVehicle() {
      // console.log(t)
      getOneVehicle(this.carInfo)
          .then(res => {
            if (res) {
              console.log("car test : ", res)
              this.carInfo.startTime = res.startTime
              this.carInfo.battery = res.battery
              this.carInfo.slocationId = res.slocationId
              this.carInfo.slocationName = res.slocationName
              this.carInfo.vehicleType = res.vehicleType
              this.$message({
                type: 'success',
                message: "get car success",
              })
            } else {
              this.$message({
                type: 'info',
                message: res.msg
              })
            }
          })
          .catch(err => {
            this.$message.error('get car fail')
          })
    },
    // time control
    dateFormat(time) {
      var date = new Date(time);
      var year = date.getFullYear();
      /* 在日期格式中，月份是从0开始的，因此要加0
      * 使用三元表达式在小于10的前面加0，以达到格式统一  如 09:11:05
      * */
      var month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
      var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
      var hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
      var minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
      var seconds = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
      // combine
      return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
    },

    closeDialog(dialog) {
      if (dialog == 'edit') {
        this.diaFormVisible = false
      } else if (dialog == 'perm') {
        this.menuAccessshow = false
      }
    },
    // return car button
    returnTheCar() {
      // console.log("index: ", index)
      // console.log("row ", row)
      this.returnForm.userId =  JSON.parse(localStorage.getItem("userdata")).id
      this.returnForm.fee = -10
      this.returnForm.vehicleId = this.carInfo.vehicleId

      this.returnForm.batteryTest = 20
      this.diaFormVisible = true
    },
    // submit function
    submitForm() {
      console.log(this.returnForm)
      returnBike(this.returnForm)
          .then(res => {
            this.diaFormVisible = false
            this.loading = false
            if (res) {
              this.carInfo.vehicleId = 0
              this.$message({
                type: 'success',
                message: 'success！'
              })
            } else {
              this.$message({
                type: 'info',
                message: res.msg
              })
            }
          })
          .catch(err => {
            this.diaFormVisible = false
            this.loading = false
            this.$message.error('pay failed！')
          })
    },
  }
}
</script>

<style scoped>
.return-container {
  border-radius: 10px;
  margin: 0px auto;
  width: 850px;
  padding: 30px 35px 15px 35px;
  background: #fff;
  opacity: 96%;
  border: 1px solid #eaeaea;
  text-align: left;
  box-shadow: 0 0 20px 2px rgba(0, 0, 0, 0.1);
}
</style>




