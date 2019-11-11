package com.hb.elevator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.chinamobile.iot.onenet.OneNetApi;
import com.chinamobile.iot.onenet.OneNetApiCallback;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hb.elevator.fragment.fragment_one;
import com.hb.elevator.fragment.fragment_three;
import com.hb.elevator.fragment.fragment_two;
import com.liang.jtablayout.tab.Tab;
import com.liang.widget.JTabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    static String APIKEY = "jYF2arDgV=Bt7pZ9ijLEYKaPE=8=";
    static String DEVICEID = "31760997";
    private ArrayList<String> floorList = new ArrayList<>();
    private Button book;
    private String selectText;
    private int selectFloor;
    private TextView tvOrderFloor;
    private LinearLayout lyMainView;
    private ListView mlistView;
    private Fragment fragment_1,fragment_2,fragment_3;
    private JTabLayout tabLayout;
    private static final String[] strs = new String[] {
            "first", "second", "third", "fourth", "fifth"};
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            this.update();
            handler.postDelayed(this, 50 * 1);// 间隔120秒
        }
        void update() {
            refreshAllData();
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        fragment_1 = new fragment_one();
        fragment_2 = new fragment_two();
        fragment_3 = new fragment_three();
        tvOrderFloor = (TextView)findViewById(R.id.textOrderFloor);
        tabLayout = findViewById(R.id.tabLayout);
        book = (Button)findViewById(R.id.book);
        OneNetApi.setAppKey(APIKEY);
        OneNetApi.init(getApplication(),true);
        changeFragment(fragment_1);
        initData();
        tabLayout.addOnTabSelectedListener(new JTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull Tab var1) {
                Toast.makeText(MainActivity.this,var1.getPosition()+ "",Toast.LENGTH_LONG).show();
                switch (var1.getPosition())
                {
                    case 0:
                        handler.removeCallbacks(runnable);
                        changeFragment(fragment_1);
                        break;
                    case 1:
                        handler.postDelayed(runnable, 50 * 1);
                        changeFragment(fragment_2);
//                        refreshAllData();
                        break;
                    case 2:
                        handler.removeCallbacks(runnable);
                        changeFragment(fragment_3);
                        break;
                }

            }

            @Override
            public void onTabUnselected(@NonNull Tab var1) {

            }

            @Override
            public void onTabReselected(@NonNull Tab var1) {

            }
        });
    }

    private void changeFragment(Fragment fragment){
        //实例化碎片管理器对象
        //选择fragment替换的部分
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        ft.replace(R.id.main_view,fragment);
        ft.commit();
    }
    public void onClick(View v){
        int floorNum = 0;
        switch (v.getId()){
            case R.id.Floor_1:
               floorNum = 1;
                break;
            case R.id.Floor_2:
                floorNum = 2;
                break;
            case R.id.Floor_3:
                floorNum  = 3;
            case R.id.textOrderFloor:
                showDialog(tvOrderFloor,floorList,0);
                break;
        }
    }

    private void initData() {
        // 填充列表
        String args[] = {"首页","发现","MyQR"};
        floorList.clear();
        for (int i = 1; i <= 3; i++) {
            floorList.add(String.format("%d层", i));
        }
        for (int i = 0; i < 3; i++){
            Tab tabItem = tabLayout.newTab();
            tabItem.setTitle(args[i]);
            tabLayout.addTab(tabItem);
        }
    }
    private void showDialog(TextView textView, ArrayList<String> list, int selected) {
        showChoiceDialog(list, textView, selected,
                new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                        selectText = item;
                        selectFloor = selectedIndex-1;
                    }
                });
    }

    private void showChoiceDialog(ArrayList<String> dataList, final TextView textView, int selected,
                                  WheelView.OnWheelViewListener listener) {
        selectText = "";
        View outerView = LayoutInflater.from(this).inflate(R.layout.dialog_wheelview, null);
        final WheelView wheelView = outerView.findViewById(R.id.wheel_view);
        wheelView.setOffset(2);// 对话框中当前项上面和下面的项数
        wheelView.setItems(dataList);// 设置数据源
        wheelView.setSeletion(selected);// 默认选中第三项
        wheelView.setOnWheelViewListener(listener);

        // 显示对话框，点击确认后将所选项的值显示到Button上
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this)
                .setView(outerView)
                .setPositiveButton("确认",
                        (dialogInterface, i) -> {
//                            textView.setText(selectFloor+"");
//                            textView.setTextColor(this.getResources().getColor(R.color.colorGreen));
                            sendOnce(DEVICEID,"floornum",selectFloor);
                            sendOnce(DEVICEID,"turnon",1);
                        })
                .setNegativeButton("取消", null).create();
        alertDialog.show();
        int green = this.getResources().getColor(R.color.colorGreen);
        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(green);
        alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(green);
    }
    public void otherButtonHandler(View view){
        switch (view.getId()){
            case R.id.Refresh:
                refreshAllData();
                break;
            case R.id.lock_elev:
                sendOnce(DEVICEID,"brake",0);
                break;
            case R.id.book:
                //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                TimePicker timePicker = new TimePicker(MainActivity.this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                timePicker.setLayoutParams(layoutParams);
                LinearLayout mLinear = new LinearLayout(MainActivity.this);
                mLinear.addView(timePicker);
                builder.setView(mLinear);
                //    设置Title的图标
//                builder.setIcon(R.drawable.ic_launcher);
                //    设置Title的内容
                builder.setTitle("设置预定时间");
                //    设置Content来显示一个信息
                //    设置一个PositiveButton
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(MainActivity.this, "positive: " + timePicker.getHour()+":"+timePicker.getMinute(), Toast.LENGTH_SHORT).show();
                        setBookTime(timePicker.getHour(),timePicker.getMinute());
                    }
                });
                //    设置一个NegativeButton
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(MainActivity.this, "negative: " + which, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
                break;
            case R.id.button_zxing:
                EditText editText = findViewById(R.id.edittext_zxing);
                ImageView imageView = findViewById(R.id.imageView_zxing);
                String count = editText.getText().toString().trim();
                if (TextUtils.isEmpty(count)) {
                    Toast.makeText(MainActivity.this, "请输入内容", Toast.LENGTH_LONG).show();
                    return;
                }
                //生成二维码显示在imageView上
                imageView.setImageBitmap(generateBitmap(count, 600, 600));
                break;
        }
    }

    public class UserBean{
        String unit;
        String unit_symbol;
        String create_time;
        String update_at;
        String id;
        int current_value;
    }
    @SuppressLint("SetTextI18n")
    private void parseJSONObjectWithGson(String userJson){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonParser().parse(userJson).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("data");
        ArrayList<UserBean> userBeanArrayList = new ArrayList<>();

        for(JsonElement user:jsonArray){
            UserBean userBean = gson.fromJson(user,new TypeToken<UserBean>(){}.getType());
            Log.e("UserBean:","ID:"+userBean.id+"Value:"+userBean.current_value);
            userBeanArrayList.add(userBean);
        }
        updateStatus(userBeanArrayList);
    }

    private void updateStatus(ArrayList<UserBean> userBeanArrayList){
        int currentFloor,TotalPeople;
        int orderHour,orderMin,orderFloor;
        int orderStatus,elevStates;
        currentFloor = userBeanArrayList.get(12).current_value;
        TotalPeople = userBeanArrayList.get(7).current_value;
        orderStatus = userBeanArrayList.get(11).current_value;
        orderFloor = userBeanArrayList.get(8).current_value;
        orderHour = userBeanArrayList.get(9).current_value;
        orderMin = userBeanArrayList.get(10).current_value;
        elevStates = userBeanArrayList.get(24).current_value;

        TextView currentFloorText = (TextView)findViewById(R.id.textCurrentFloor);
        TextView totalPeopleText = (TextView)findViewById(R.id.textTotalPeople);
        TextView orderStatusText = (TextView)findViewById(R.id.textOrderStatus);
        TextView orderTimeText = (TextView)findViewById(R.id.OrderTimeText);
        TextView orderFloorText = (TextView)findViewById(R.id.textOrderFloor);
        TextView elevStateText = (TextView)findViewById(R.id.textElevState);
        DecimalFormat decimalFormat = new DecimalFormat("00");

        if(currentFloorText!=null) {
            currentFloorText.setText(currentFloor + "层");
            totalPeopleText.setText(TotalPeople + "Kg");
            orderTimeText.setText(decimalFormat.format(orderHour) + ":" + decimalFormat.format(orderMin));
            orderFloorText.setText(orderFloor + "层");
//
            if (orderStatus==1) {
                orderStatusText.setTextColor(Color.rgb(0, 255, 255));
                orderStatusText.setText("受理中");
            } else if(orderStatus==0){
                orderStatusText.setTextColor(Color.rgb(0, 255, 0));
                orderStatusText.setText("成功");
            }

            if(elevStates==0){
                elevStateText.setTextColor(Color.rgb(255, 0, 0));
                elevStateText.setText("检修中...");
            }else if(elevStates==1){
                elevStateText.setTextColor(Color.rgb(0, 255, 0));
                elevStateText.setText("正常运行");
            }
        }
    }

    private void setBookTime(int hour,int min){
        sendOnce(DEVICEID,"hour",hour);
        sendOnce(DEVICEID,"minute",min);
        sendOnce(DEVICEID,"turnon",1);
    }


    private void sendOnce(String deviceId, String datastream,int value) {
        JSONObject request = new JSONObject();
        try {
            request.putOpt(datastream, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OneNetApi.addDataPoints(deviceId, "3", request.toString(), new OneNetApiCallback() {
            @Override
            public void onSuccess(String response) {

            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshAllData() {
        OneNetApi.queryMultiDataStreams(DEVICEID, new OneNetApiCallback() {
            @Override
            public void onSuccess(String response) {
                parseJSONObjectWithGson(response);
                Log.e("HSUError", response);
            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }
    /**
     * 生成固定大小的二维码(不需网络权限)
     *
     * @param content 需要生成的内容
     * @param width   二维码宽度
     * @param height  二维码高度
     * @return
     */
    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}