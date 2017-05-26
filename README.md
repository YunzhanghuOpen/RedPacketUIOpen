# 云账户支付宝版红包API文档 (Android)

## 1.初始化红包SDK方法

```java
RedPacket.getInstance().initRedPacket(context, authMethod, callback)
```

- 方法说明

  该方法用于初始化红包SDK，红包SDK通过该方法获取初始化红包Token时所需的参数以及当前用户信息。建议在Application的onCreate方法中调用。


- 参数说明

| 参数名称       |          参数类型           |     参数说明     |
| :--------- | :---------------------: | :----------: |
| context    |         Context         |     上下文      |
| authMethod |         String          |    授权方法常量    |
| callback   | RPInitRedPacketCallback | 初始化红包SDK回调接口 |

- authMethod传值说明
  - 签名方式：RPConstant.AUTH_METHOD_SIGN
  - 环信IM：RPConstant.AUTH_METHOD_EASE_MOB
  - 容联云通讯IM：RPConstant.AUTH_METHOD_YTX
- RPInitRedPacketCallback接口回调函数说明

| initTokenData(RPValueCallback<TokenData> callback) |
| :--------------------------------------- |
| 该方法用于初始化TokenData，在红包Token不存在、切换用户或红包Token过期时调用。TokenData是请求红包Token所需要的数据模型，建议在该方法中异步向App服务器获取相关参数，以保证数据的有效性；注意不要本地获取TokenData所需的参数，本地获取会有安全隐患。 |
| **RedPacketInfo initCurrentUserSync()**  |
| 该方法用于初始化当前用户信息，在进入红包相关页面时调用，需同步获取。       |

- 调用示例

```java
RedPacket.getInstance().initRedPacket(context, RPConstant.AUTH_METHOD_SIGN, new RPInitRedPacketCallback() {

        @Override
        public void initTokenData(RPValueCallback<TokenData> callback) {
           // TokenData中的参数需要开发者向App Server开发的签名接口获取 
           TokenData tokenData = new TokenData();
           // App ID
           tokenData.authPartner = "App ID";
           // appUserId App的当前用户id
           tokenData.appUserId = "App UserId";
           // 时间戳，需使用参与生成签名时的时间戳，不可使用本地获取的时间戳。
           tokenData.timestamp = "时间戳精确到秒";
           // 签名串
           tokenData.authSign = "App Server返回的签名串";
           // 赋值后回调给红包SDK
           callback.onSuccess(tokenData);
        }

        @Override
        public RedPacketInfo initCurrentUserSync() {
           // 这里需要同步设置App的当前用户id、昵称和头像url
           RedPacketInfo currentUserInfo = new RedPacketInfo();
           currentUserInfo.currentUserId = "Current user id";
           currentUserInfo.currentNickname = "Current user nickname";
           currentUserInfo.currentAvatarUrl = "Current user avatar url";
           return currentUserInfo;
        }
});
```

- **[服务端签名接口开发](https://new.yunzhanghu.com/intro/server.html)** 
- **[初始化SDK参考示例](https://github.com/YunzhanghuOpen/AndroidRedPacketDemo/blob/master/RedPacketDemo/app/src/main/java/com/yunzhanghu/redpacketdemo/DemoApplication.java)**

## 2.初始化红包Token方法

```java
RedPacket.getInstance().initRPToken(currentUserId, callback)
```

- 方法说明

  在调用红包服务前需使用该方法获取红包Token。若Token已存在且有效该方法会立即返回；若不存在、切换用户或者红包token已过期，则会重新请求并返回。

- 参数说明

| 参数名称          | 参数类型            | 参数说明        |
| ------------- | --------------- | ----------- |
| currentUserId | String          | 当前用户id      |
| callback      | RPTokenCallback | 红包Token回调接口 |

- 调用示例

```java
RedPacket.getInstance().initRPToken(currentUserId, new RPTokenCallback() {
            @Override
            public void onTokenSuccess() {
                // 请求token成功
            }

            @Override
            public void onSettingSuccess() {
                // 请求配置信息成功，自定义UI可不处理此回调
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
               // 请求token或配置信息失败
            }
        });
```



## 3.发红包相关接口

- 接口说明

  发红包相关接口封装在SendPacketPresenter中，可调用相关方法请求数据。

### 3.1 preparePayment(String amount)

- 该方法用于获取支付宝支付的订单信息及用户的授权状态。

| 参数名称   | 参数类型   | 参数说明 |
| ------ | ------ | ---- |
| amount | String | 红包金额 |

### 3.2 uploadAuthInfo(String authCode, String userId)

- 该方法用于上传用户的支付宝授权信息到云账户服务端。

| 参数名称     | 参数类型   | 参数说明                 |
| -------- | ------ | -------------------- |
| authCode | String | 支付宝授权码，支付宝SDK授权接口返回  |
| userId   | String | 支付宝用户id，支付宝SDK授权接口返回 |

### 3.3 sendRedPacket(RedPacketInfo redPacketInfo)

- 该方法用于请求云账户发红包接口，调用该接口成功后可发送红包消息到IM通道。

#### RedPacketInfo赋值参数

- 单聊红包

| 参数名称              | 参数类型   | 参数说明                      |
| ----------------- | ------ | ------------------------- |
| redPacketId       | String | 红包id                      |
| tradeNo           | String | 支付宝订单流水号                  |
| redPacketAmount   | String | 红包金额                      |
| redPacketGreeting | String | 红包祝福语                     |
| senderNickname    | String | 发送者昵称                     |
| senderAvatarUrl   | String | 发送者头像url                  |
| receiverId        | String | 接收者id                     |
| receiverNickname  | String | 接收者昵称                     |
| receiverAvatarUrl | String | 接收者头像url                  |
| redPacketType     | String | 红包类型(小额随机红包传入) （见注1）  |

- 群聊红包

| 参数名称              | 参数类型   | 参数说明          |
| ----------------- | ------ | ------------- |
| redPacketId       | String | 红包id          |
| tradeNo           | String | 支付宝订单流水号      |
| redPacketAmount   | String | 红包金额          |
| redPacketGreeting | String | 红包祝福语         |
| senderNickname    | String | 发送者昵称         |
| senderAvatarUrl   | String | 发送者头像url      |
| groupId           | String | 群id           |
| totalCount        | int    | 红包个数          |
| redPacketType     | String | 红包类型（见注1） |

- 群专属红包

| 参数名称              | 参数类型   | 参数说明          |
| ----------------- | ------ | ------------- |
| redPacketId       | String | 红包id          |
| tradeNo           | String | 支付宝订单流水号      |
| redPacketAmount   | String | 红包金额          |
| redPacketGreeting | String | 红包祝福语         |
| senderNickname    | String | 发送者昵称         |
| senderAvatarUrl   | String | 发送者头像url      |
| groupId           | String | 群id           |
| totalCount        | int    | 红包个数          |
| redPacketType     | String | 红包类型 (见注1) |
| receiverId        | String | 专属红包接收者id     |
| receiverNickname  | String | 专属红包接收者昵称     |
| receiverAvatarUrl | String | 专属红包接收者头像url  |

- 注1：红包类型及对应的常量见下表

| 红包类型         | 对应的常量值                                   |
| ------------ | ---------------------------------------- |
| 群拼手气红包       | RPConstant.RED_PACKET_TYPE_GROUP_RANDOM  |
| 群普通红包        | RPConstant.RED_PACKET_TYPE_GROUP_AVERAGE |
| 群专属红包        | RPConstant.RED_PACKET_TYPE_GROUP_EXCLUSIVE |
| 小额随机红包（仅限单聊） | RPConstant.RED_PACKET_TYPE_SINGLE_RANDOM |
| 营销红包         | RPConstant.RED_PACKET_TYPE_ADVERTISEMENT |

### SendPacketContract.View回调接口

#### onPreparePaymentSuccess(String tradeNo, String orderInfo)

调用`preparePayment(String myAmount)`方法成功且用户已绑定支付宝账号时，回调此方法。

- 参数说明

| 参数名称      | 参数类型   | 参数说明          |
| --------- | ------ | ------------- |
| tradeNo   | String | 交易流水号         |
| orderInfo | String | 请求支付宝支付时的订单信息 |

#### onUserUnauthorized(String authInfo)

调用`preparePayment(String myAmount)`方法成功且用户未绑定支付宝账号时，回调此方法。

- 参数说明

| 参数名称     | 参数类型   | 参数说明             |
| -------- | ------ | ---------------- |
| authInfo | String | 请求支付宝授权接口需要的授权信息 |

#### onUploadAuthInfoSuccess()

调用`uploadAuthInfo(String authCode, String userId)`方法成功时，回调此方法。此方法返回视为用户支付宝授权成功。

#### onSendPacketSuccess(String redPacketId)

调用`sendRedPacket(RedPacketInfo redPacketInfo)`方法成功时，回调此方法。

- 参数说明

| 参数名称        | 参数类型   | 参数说明 |
| ----------- | ------ | ---- |
| redPacketId | String | 红包id |

#### onError(String code, String message)

调用**3.1-3.3**中的方法出现错误时，回调此方法。错误码可参考开源UI中的处理。

## 4.查询红包状态接口

- 接口说明

  查询红包状态接口封装在CheckPacketStatusPresenter中，可调用相关方法查询红包状态。

### checkRedPacketStatus(String redPacketId)

- 该方法用于拆红包之前获取红包状态，以此为依据展示不同的页面。

| 参数名称        | 参数类型   | 参数说明 |
| ----------- | ------ | ---- |
| redPacketId | String | 红包id |

### CheckPacketContract.View回调接口

#### onCheckStatusSuccess(RedPacketInfo redPacketInfo, boolean isShowDialog)

调用`checkRedPacketStatus(String redPacketId)` 方法返回成功时，回调此方法。具体有以下情形：

​    **isShowDialog为true时展示拆红包对话框的UI。**

- 当前用户为红包消息发送方时，以下情况回调此方法：
  - 该红包类型为拼手气群红包，且红包发送者未领取该红包。
- 当前用户为红包消息接收方时，以下情况回调此方法：
  - 该红包类型为**非**小额随机红包，且红包消息接收者未领取该红包。

​    **isShowDialog为false时展示红包详情页的UI。**

- 当前用户为红包消息发送方时，以下情况回调此方法：
  - 该红包类型为单聊红包；
  - 该红包类型为普通群红包；
  - 该红包类型为专属群红包；
  - 该红包类型为拼手气群红包且发送者已领取过该红包。


- 当前用户为红包消息接收方时，以下情况回调此方法：
  - 该红包类型为小额随机红包；
  - 红包消息接收者已领取过该红包。
- **以上逻辑已封装在红包SDK内部，开发者只需实现回调进行UI展示即可。**

#### onPacketExpired(String message)

调用`checkRedPacketStatus(String redPacketId)` 方法返回错误且错误码为**3011**(代表红包已过期或不存在)时，回调此方法。

#### onError(String code, String message)

调用`checkRedPacketStatus(String redPacketId)` 方法返回其他错误时，回调此方法。

## 5. 拆红包相关接口

- 接口说明

  拆红包相关接口封装在ReceivePacketPresenter中，在执行拆红包操作时调用。

### 5.1 receiveRedPacket(String redPacketId, String redPacketType)

- 该方法用于领取红包，领取成功后可发送红包领取的回执消息到IM通道。

| 参数名称          | 参数类型   | 参数说明 |
| ------------- | ------ | ---- |
| redPacketId   | String | 红包id |
| redPacketType | String | 红包类型 |

### 5.2 uploadAuthInfo(String authCode, String userId)

- 该方法用于上传用户的支付宝授权信息到云账户服务端。

| 参数名称     | 参数类型   | 参数说明                 |
| -------- | ------ | -------------------- |
| authCode | String | 支付宝授权码，支付宝SDK授权接口返回  |
| userId   | String | 支付宝用户id，支付宝SDK授权接口返回 |

### ReceivePacketContract.View回调接口

#### onReceivePacketSuccess(String redPacketId, String myAmount, String landingPage)

调用` receiveRedPacket(String redPacketId, String redPacketType)`方法领取红包成功时，回调此方法。

- 参数说明

| 参数名称        | 参数类型   | 参数说明                 |
| ----------- | ------ | -------------------- |
| redPacketId | String | 红包id                 |
| myAmount    | String | 领取的红包金额              |
| landingPage | String | 营销红包着陆页url，非营销红包返回为空 |

#### onUserUnauthorized(String authInfo)

调用` receiveRedPacket(String redPacketId, String redPacketType)`方法返回错误且错误码为**60201**(代表用户未授权)时，回调此方法。

- 参数说明

| 参数名称     | 参数类型   | 参数说明             |
| -------- | ------ | ---------------- |
| authInfo | String | 请求支付宝授权接口需要的授权信息 |

#### onUploadAuthInfoSuccess()

调用`uploadAuthInfo(String authCode, String userId)`方法成功时，回调此方法。此方法返回视为用户支付宝授权成功。

#### onRedPacketSnappedUp(String redPacketType)

调用` receiveRedPacket(String redPacketId, String redPacketType)`方法返回错误且错误码为**3013**(代表红包已抢完)时，回调此方法。

- 参数说明

| 参数名称          | 参数类型   | 参数说明 |
| ------------- | ------ | ---- |
| redPacketType | String | 红包   |

#### onRedPacketAlreadyReceived()

调用` receiveRedPacket(String redPacketId, String redPacketType)`方法返回错误且错误码为**3014**(代表你已领取过该红包)时，回调此方法。此回调方法用于支持具有多端登录功能的IM，不支持此功能的可不处理此回调方法。

#### onError(String code, String message)

调用` receiveRedPacket(String redPacketId, String redPacketType)`方法返回其他错误时，回调此方法。



## 6. 红包记录页面相关接口

- 接口说明

  红包记录相关接口封装在RPRecordPresenter中，用于获取红包记录、解绑支付宝账户等相关数据的操作。

### 6.1 getRecordList(int eventTag, int offset, int length)

- 该方法用于获取红包记录列表数据。

| 参数名称     | 参数类型 | 参数说明               |
| -------- | ---- | ------------------ |
| eventTag | int  | 获取首页数据或加载更多标识 （见注） |
| offset   | int  | 红包记录列表起始索引         |
| length   | int  | 红包记录列表单页长度         |

- 注：enventTag值为RPConstatn.EVENT_REFRESH_DATA或RPConstant.EVENT_LOAD_MORE_DATA，对应的含义分别为获取首页数据和加载更多数据。

### 6.2 getAuthInfo()

- 该方法用于获取支付宝授权时所需要的授权参数。

### 6.3 uploadAuthInfo(String authCode, String userId)

- 该方法用于上传用户的支付宝授权信息到云账户服务端。

| 参数名称     | 参数类型   | 参数说明                 |
| -------- | ------ | -------------------- |
| authCode | String | 支付宝授权码，支付宝SDK授权接口返回  |
| userId   | String | 支付宝用户id，支付宝SDK授权接口返回 |

### 6.4 getAliUserInfo()

- 该方法用于获取用户的支付宝信息。

### 6.5 unBindAliUser()

- 该方法用于解绑用户绑定的支付宝账户。

### RPRecordContract.View回调接口

#### onRecordListSuccess(RedPacketInfo headerInfo, ArrayList<RedPacketInfo> listInfo, PageInfo pageInfo)

调用`getRecordList(int eventTag, int offset, int length)`方法返回成功且eventTag传入RPConstatn.EVENT_REFRESH_DATA时，回调此方法。

#### onMoreRecordListSuccess(ArrayList<RedPacketInfo> listInfo, PageInfo pageInfo)

调用`getRecordList(int eventTag, int offset, int length)`方法返回成功且eventTag传入RPConstant.EVENT_LOAD_MORE_DATA时，回调此方法。

#### onUserUnauthorized(String authInfo)

调用`getAuthInfo()`方法返回成功时，回调此方法。

- 参数说明

| 参数名称     | 参数类型   | 参数说明             |
| -------- | ------ | ---------------- |
| authInfo | String | 请求支付宝授权接口需要的授权信息 |

#### onAliUserInfoSuccess(String userInfo, boolean isRefreshed)

调用`getAliUserInfo()`方法返回成功时，回调此方法。

- 参数说明

| 参数名称        | 参数类型    | 参数说明           |
| ----------- | ------- | -------------- |
| userInfo    | String  | 支付宝用户信息        |
| isRefreshed | boolean | 是否刷新用户支付宝信息的标识 |

#### onUploadAuthInfoSuccess()

调用`uploadAuthInfo(String authCode, String userId)`方法成功时，回调此方法。此方法返回视为用户支付宝授权成功。

#### onUnboundAliSuccess()

调用`unBindAliUser()`方法成功时，回调此方法。此方法返回视为用户支付宝授权成功。

#### onError(String code, String message)

调用**6.1-6.5**中的方法返回错误时，回调此方法。

## 7.红包详情接口

- 接口说明

  红包详情接口封装在RedPacketDetailPresenter，用于获取红包详情数据。

### getPacketDetail(String redPacketId, String latestReceiverId, int offset, int length)

| 参数名称             | 参数类型   | 参数说明       |
| ---------------- | ------ | ---------- |
| redPacketId      | String | 红包id       |
| latestReceiverId | String | 接收者id（见注）  |
| offset           | int    | 红包详情列表起始索引 |
| length           | int    | 红包详情列表单页长度 |

- 注：latestReceiverId需要传入领取人列表中第一条数据的receiverId，第一次获取时可传入空字符串。

### RedPacketDetailContract.View回调接口

#### onSinglePacketDetailSuccess(RedPacketInfo redPacketInfo)

调用`getPacketDetail(String redPacketId, String latestReceiverId, int offset, int length)`方法成功且红包类型为单聊红包时，回调此方法。

#### onGroupPacketDetailSuccess(RedPacketInfo headerInfo, ArrayList<RedPacketInfo> listInfo, PageInfo pageInfo)

调用`getPacketDetail(String redPacketId, String latestReceiverId, int offset, int length)`方法成功且红包类型为群聊红包时，回调此方法。

#### onError(String code, String message)

调用`getPacketDetail(String redPacketId, String latestReceiverId, int offset, int length)`方法返回错误时，回调此方法。

