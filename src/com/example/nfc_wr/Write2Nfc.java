package com.example.nfc_wr;

import java.io.IOException;
import java.util.ArrayList;



import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author 瑞泽_Zerui
 * 将数据写入NFC标签
 * 注：写入数据关键是弄清数据的封装方法，即NdefRecord和NdefMessage几个关键类,具体可参见开发文档或网上资料
 */
public class Write2Nfc extends Activity {
	private EditText editText;
	private EditText editText1;
	private EditText editText2;
	private TextView noteText;
	private Button wButton;
	private IntentFilter[] mWriteTagFilters;
	private NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	String[][] mTechLists;
	private Boolean ifWrite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write_ifo);
		init();
		displayControl(false);
		System.out.println("0....");
	}

	private void init() {
		// TODO Auto-generated method stub
		ifWrite = false;
		editText = (EditText) findViewById(R.id.editText);
		editText1= (EditText) findViewById(R.id.editText1);
		editText2=(EditText) findViewById(R.id.editText2);
		wButton = (Button) findViewById(R.id.writeBtn);
		noteText=(TextView)findViewById(R.id.noteText);
		
		wButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ifWrite = true;
				displayControl(true);
			}
		});
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		ndef.addCategory("*/*");
		mWriteTagFilters = new IntentFilter[] { ndef };
		mTechLists = new String[][] { new String[] { NfcA.class.getName() },
				new String[] { NfcF.class.getName() },
				new String[] { NfcB.class.getName() },
				new String[] { NfcV.class.getName() } };
	}
	public void displayControl(Boolean ifWriting){
		if(ifWriting){			
			noteText.setVisibility(View.VISIBLE);
			editText.setVisibility(View.INVISIBLE);
			editText1.setVisibility(View.INVISIBLE);
			editText2.setVisibility(View.INVISIBLE);
			wButton.setVisibility(View.INVISIBLE);
			
			return;
		}
		noteText.setVisibility(View.INVISIBLE);
		editText.setVisibility(View.VISIBLE);
		editText1.setVisibility(View.VISIBLE);
		editText2.setVisibility(View.VISIBLE);
		wButton.setVisibility(View.VISIBLE);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		nfcAdapter.enableForegroundDispatch(this, pendingIntent,
				mWriteTagFilters, mTechLists);
		System.out.println("1....");
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		System.out.println("1.5....");
		String text0 = editText.getText().toString();
		String text1 = editText1.getText().toString();
		String text2 = editText2.getText().toString();
		ArrayList<String> al=new ArrayList<String>();
		al.add(text0);
		al.add(text1);
		al.add(text2);
		
		if (text0 == null || text0==""  || 
				text1 == null || text1 =="" ||
				text2 == null || text2 ==""        ) {
			Toast.makeText(Write2Nfc.this, "数据不能为空!",
					Toast.LENGTH_SHORT).show();
			System.out.println("2....");

			return;
		}
		if (ifWrite == true) {
			System.out.println("2.5....");
			if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())||
					NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
				Tag tag =intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		    	MifareClassic mfc= MifareClassic.get(tag);
		    	
		    	if(mfc!=null){
		    		System.out.println("开始连接mfc");
		    		try
		    		
		    		    {
		    		
		    		        mfc.connect();
		    		        System.out.println("连接mfcc成功");
		    		        boolean auth = false;
		    		
		    		       
		    		        
		    		        
		    		        int blockIndex=mfc.sectorToBlock(3);
		    		        auth = mfc.authenticateSectorWithKeyA(3, MifareClassic.KEY_DEFAULT);
		    		        System.out.println("auth=="+auth);
		    		        if (auth)
		    		
		    		            {

								
		    		                // the last block of the sector is used for KeyA and KeyB cannot be overwritted
		    		        	for(int j=0;j<3;j++){
		    		        		String text=al.get(j);
								int textlength=text.getBytes().length;
								if(textlength>16){
									Toast.makeText(Write2Nfc.this, "输入的字数太多了,必须为16个字节，你现在输入了"+text.getBytes().length+"个字节", Toast.LENGTH_SHORT).show();
									
								}
								else{
									
									for(int i=0;i<16;i++){
										
										if(text.getBytes().length==16){
											 System.out.println("写入的是："+text.getBytes());
											 
												 
					    		                mfc.writeBlock(blockIndex+j, text.getBytes());//必须为16字节不够自己补0
					    		                
											 
					    		               
					    		
					    		                Toast.makeText(Write2Nfc.this, "写入成功，字数必须为16个字节，程序帮你补充了"+i+"个0", Toast.LENGTH_SHORT).show();
					    		                break;
											  }
										//不够16个字节就补零
										text=text+"0";
										
									}
									
								}
								
		    		        	}
		    		        	
		    		
		    		            }
		    		        
		    		        
		    		    } catch (IOException e) {
		    		    	Toast.makeText(Write2Nfc.this, "写入失败", Toast.LENGTH_SHORT).show();
		         e.printStackTrace();

		    		    } finally
		    	
		    		    {
		    	
		    		        try
		    
		    		            {
		    	
		    		                mfc.close();
		    	
		    		            } catch (IOException e)
		    	
		    		            {
		    		
		    		                // TODO Auto-generated catch block
		    		            	Toast.makeText(Write2Nfc.this, "写入失败", Toast.LENGTH_SHORT).show();
		    		                e.printStackTrace();
		    		
		    		            }
		    		
		    		    }
		    	}
		    	else{
		    		Toast.makeText(Write2Nfc.this, "写入失败", Toast.LENGTH_SHORT).show();
		    		System.out.println("mfc为null");
		    	}
		    	
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
		
				
				
				
			}
		}
	}

}
