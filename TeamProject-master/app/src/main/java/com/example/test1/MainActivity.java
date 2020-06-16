package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private Marker marker; //마커 삭제를 위한 마커객체 생성

    private FrameLayout frame; //정보창 프레임
    private EditText inputAddr; //입력받은 주소값
    private Button btn_go,btn_info; //검색버튼, 정보창

    private double longitude; //경도
    private double latitude; //위도


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //지도구현
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        inputAddr = (EditText) findViewById(R.id.inputAddr);
        btn_go = (Button) findViewById(R.id.btn_go);
        btn_info = (Button)findViewById(R.id.btn_info);
        frame = (FrameLayout)findViewById(R.id.frame);


        final Geocoder geocoder = new Geocoder(this); //입력받은 주소 값 위도경도 값으로 변환해주는 거

        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Address> list = null;
                String str = inputAddr.getText().toString();

                try {
                    list = geocoder.getFromLocationName(str, 10);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("test", "입출력 오류");
                }

                marker.remove(); //이전의 마커 삭제
                if (list != null) {
                    if (list.size() == 0) {
                        //입력한 주소의 위도경도 값이 없다면
                        Toast.makeText(MainActivity.this,"해당되는 주소 정보는 없습니다",Toast.LENGTH_SHORT).show();

                    } else {

                        Address address = list.get(0);
                        longitude = address.getLongitude(); //경도
                        latitude = address.getLatitude(); //위도
                        LatLng latLng = new LatLng(latitude, longitude);

                        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));

                    }
                }

                //DB에 위도경도 값 보내기(미완성)
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("정보");


                LocationDTO locationDTO = new LocationDTO(); //위도, 경도 값 세트로 DB에 올리기 위해서, LocationDTO class 객체 생성
                locationDTO.Lat = latitude;
                locationDTO.Long = longitude;
                databaseReference.child(str).setValue(locationDTO);

                databaseReference.child(str).child("기타정보").child("uri").setValue("www.naver.com");
                databaseReference.child(str).child("기타정보").child("사용자ID").setValue("hannaKim");
                databaseReference.child(str).child("기타정보").child("업로드 시간").setValue("12:50");

                //해야할 거 : 정보 중복체크, uri, userId, 업로드 시간 class 생성 후 업로드하기
            }
        });


        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(frame.getVisibility() == View.GONE) { //btn_info 버튼 한번 눌렀을때

                    frame.setVisibility((View.VISIBLE));
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    Info_Fragment info_fragment = new Info_Fragment();
                    transaction.replace(R.id.frame, info_fragment);
                    transaction.commit();
                    //해야할 거 : fragment 장소이름 text 변경하기
                }
                else{ //btn_info 버튼 두번 눌렀을때
                    frame.setVisibility(View.GONE);
                }

            }
        });

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;

        LatLng Seoul = new LatLng(37.551036, 126.990899); //처음 지도 켰을때 위치
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Seoul);
        markerOptions.title("서울");
        markerOptions.snippet("남산공원");
        marker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(Seoul));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

    }
}
