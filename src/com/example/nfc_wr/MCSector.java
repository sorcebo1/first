package com.example.nfc_wr;



public class MCSector {
	// each sector has four block
	public final static int BLOCKCOUNT=4;
	//sector id
	public int sectorid;
	//sector keyA
	public int sectorkeyA;
	//sector keyB
	public int sectorkeyB;
	
	public MCBlock[] blocks=new MCBlock[BLOCKCOUNT];
	
	

}
