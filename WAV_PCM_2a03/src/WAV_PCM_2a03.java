import java.io.File;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.io.PrintWriter;

public class WAV_PCM_2a03 {

  public static void main (String[] args) {

	try {
		DataInputStream inputWaveFile = new DataInputStream(
										new BufferedInputStream(
										new FileInputStream(new File(args[0]))));	
		/*
			
					= WAVE HEADER INFORMATION =
					
		endianess		OFFSET   FIELDNAME     SIZE
				B		0        ChunkID       4
				L		4        ChunkSize     4
				B		8        Format        4
						------------------------
				L		12       Subchunk1ID   4
				L		16       Subchunk1Size 4
				L		20       AudioFormat   2
				L		22       NumChannels   2
				L		24       SampleRate    4
				L		28       ByteRate      4
				L		32       BlockAlign    2
				L		34       BitsPerSample 2
						------------------------
				B		36       Subchunk2ID   4
				L		40       Subchunk2Size 4
				L		44       data          Subchunk2Size
						------------------------
						
		*/
		
		// WE GOTTA MAKE SURE IT'S MONO, AND 16 BIT
		// AND 44100 SAMPLE RATE OR ELSE THIS AINT GONNA
		// WORK YO read(byte[] b, int off, int len)
		
		byte[] wav_format = new byte[17];
		inputWaveFile.skipBytes(8);
		wav_format[0] = inputWaveFile.readByte();
		wav_format[1] = inputWaveFile.readByte();
		wav_format[2] = inputWaveFile.readByte();
		wav_format[3] = inputWaveFile.readByte();
		
		char[] wav_format_chars = new char[4];
		for (int i=0; i<4; i++) {
			wav_format_chars[i] = (char)wav_format[i];
		}
		String charToString = new String(wav_format_chars);
		
		System.out.println("Read Datatype was [" + charToString + "] (looking for WAVE)");
		if (!charToString.equals("WAVE")) {
			System.out.println("::: ERROR: NEEDS TO BE A WAVE FILE");
			throw new Exception();
		}
		inputWaveFile.skipBytes(22-12);
		wav_format[6] = inputWaveFile.readByte();
		wav_format[5] = inputWaveFile.readByte();
		long waveChannels = ((int)wav_format[5])*256 + ((int)wav_format[6]);
		System.out.println("Detected Channels was [" + waveChannels + "] (looking for 1 eg. MONO)");
		if (waveChannels != 1) {
			System.out.println("::: ERROR: NEEDS TO BE MONO (use audacity)");
			throw new Exception();
		}
		wav_format[10] = inputWaveFile.readByte();
		wav_format[9]  = inputWaveFile.readByte();
		wav_format[8]  = inputWaveFile.readByte();
		wav_format[7]  = inputWaveFile.readByte();
		int srate1 = (int)wav_format[9];
		int srate2 = (int)wav_format[10];
		srate1 = srate1 & 0xFF;
		srate2 = srate2 & 0xFF;
		long sampleRate = (srate1)*256 + (srate2);
		System.out.println("Detected Samplerate was [" + sampleRate + "] (looking for 8700)");
		if (sampleRate != 8700) {
			System.out.println("::: ERROR: SAMPLE RATE NOT 8700 (use audacity)");
			throw new Exception();
		}
		inputWaveFile.skipBytes(6);
		wav_format[12] = inputWaveFile.readByte();
		wav_format[11] = inputWaveFile.readByte();
		int bps1 = (int)wav_format[11];
		int bps2 = (int)wav_format[12];
		bps1 = bps1 & 0xFF;
		bps2 = bps2 & 0xFF;
		long bitsPerSample = (bps1)*256 + (bps2);
		System.out.println("Detected Bits per Sample was [" + bitsPerSample + "] (looking for 16)");
		if (bitsPerSample != 16) {
			System.out.println("::: ERROR: NEEDS TO BE 16 BIT AUDIO (use audacity)");
			throw new Exception();
		}
		inputWaveFile.skipBytes(4);
		wav_format[16] = inputWaveFile.readByte();
		wav_format[15] = inputWaveFile.readByte();
		wav_format[14] = inputWaveFile.readByte();
		wav_format[13] = inputWaveFile.readByte();
		int dataSize_1 = ((int)wav_format[13]) & 0xFF;
		int dataSize_2 = ((int)wav_format[14]) & 0xFF;
		int dataSize_3 = ((int)wav_format[15]) & 0xFF;
		int dataSize_4 = ((int)wav_format[16]) & 0xFF;
		String stringRepOfBinary =	Integer.toBinaryString(0x100 | dataSize_1).substring(1) +
									Integer.toBinaryString(0x100 | dataSize_2).substring(1) +
									Integer.toBinaryString(0x100 | dataSize_3).substring(1) +
									Integer.toBinaryString(0x100 | dataSize_4).substring(1);
		System.out.println("datasize: " + stringRepOfBinary);
		byte[] dataSize = new byte[4];
		dataSize[0] = wav_format[13];
		dataSize[1] = wav_format[14];
		dataSize[2] = wav_format[15];
		dataSize[3] = wav_format[16];
		BigInteger dataSize_BI = new BigInteger(dataSize);
		System.out.println("datasize: " + dataSize_BI.toString() + " bytes");
		//collect all the samples now!
		System.out.println("collecting samples...");
		ArrayList<Short> samples = new ArrayList<Short>();		
		long samplesToCollect = dataSize_BI.longValue();
		System.out.println("collecting a total of " + samplesToCollect/2 + " samples");
		for(long i=0; i<samplesToCollect/2; i++) {
			short pre_processed_sample = inputWaveFile.readShort();
			short processed_sample     = (short)(((pre_processed_sample&0xff)<<8)+((pre_processed_sample&0xff00)>>8));
			samples.add(processed_sample);
			if (i%4000 == 0)
				System.out.printf(processed_sample + " ");
		}
		System.out.println("\ndownsampling....");
		// downsample to 8bit now;
		for(int i=0; i<samples.size(); i++) {
			samples.set(i, (short)(samples.get(i)/(256))); // converting to 8bit
			if (i%4000 == 0)
				System.out.printf(samples.get(i) + " ");
		}
		System.out.println("\ndownsampling cleanup....");
		for(int i=0; i<samples.size(); i++) {
			if (samples.get(i)%2 == 1)
				samples.set(i, (short)(samples.get(i)-1));
			if (samples.get(i)%2 == -1)
				samples.set(i, (short)(samples.get(i)+1));
			if (i%4000 == 0)
				System.out.printf(samples.get(i) + " ");
		}
		System.out.println("\nconverting 8bit to 7bit....");
		for(int i=0; i<samples.size(); i++) {
			samples.set(i, (short)(samples.get(i)/2));
			if (i%4000 == 0)
				System.out.printf(samples.get(i) + " ");
		}
		System.out.println("\nfixing ranges...");
		for(int i=0; i<samples.size(); i++) {
			samples.set(i, (short)(samples.get(i)+64));
			if (i%4000 == 0) {
				StringBuilder sb = new StringBuilder();
				sb.append(Integer.toHexString((int)samples.get(i)));
				if (sb.length() < 2) {
					sb.insert(0, '0'); // pad with leading zero if needed
				}
				String hex = sb.toString();
				System.out.printf("$" + hex + " ");
			}
		}
		
		System.out.println("\nTotal samples: " + samples.size());
		String outputFile = "E ";
		for (int i=0; i<samples.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(Integer.toHexString((int)samples.get(i)));
			if (sb.length() < 2) {
				sb.insert(0, '0'); // pad with leading zero if needed
			}
			String hex = sb.toString();
			outputFile = outputFile + "y$4011,$" + hex + " ";
		}
		PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
		writer.println(outputFile);
		writer.close();
		
	} catch (Exception e) {
		System.out.println("- Hey, make sure you're using a Microsoft 16 Bit");
		System.out.println("- wave file or else this wont work. (double check");
		System.out.println("- to see if the file exists too haha lol)");
		System.out.println();
		System.out.println("- r.i.p. WAV_PCM_2a03.class \n");
		e.printStackTrace();
	}
	
	
	
  }

}


/*	
		23:35 <Xyz_39808>
			Ideally you'd just take a wave, downsample to 8bit,
			then round up every odd valued sample and divide by
			two (making it 7bit), then stream that after a
			bunch of y$4011,$__ commands  						

		01:33 <savestate_loves_malmen>
			Xyz_39808, what's the fastest samplerate it can do
			
		01:33 <savestate_loves_malmen>
			ppmck
			
		01:42 <Xyz_39808>
			I have no clue
			
		01:47 <Xyz_39808>
			looks to be ~9480hz
			
	*/