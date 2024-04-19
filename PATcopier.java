import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.*;


public class PATcopier
{
	

	public static Byte[] to2Bytes(short s) {
		return new Byte[]{(byte)(s & 0x00FF),(byte)((s & 0xFF00)>>8)};
	}

	public static Byte[] to2Bytes(int s) {
		return new Byte[]{(byte)(s & 0x00FF),(byte)((s & 0xFF00)>>8)};
	}

	public static Byte[] to4Bytes(float s) {
		byte[] in = ByteBuffer.allocate(4).putFloat(s).array();
		Byte[] out = new Byte[in.length];
		for (int i=0;i<out.length;i++){
			out[i]=in[out.length-1-i];
		}
		return out;
	}
		

	public static byte[] floatToByteArray(float value) {
        int intBits =  Float.floatToIntBits(value);
        return new byte[] {
          (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
    }

	byte buff[] = new byte[1];
	public int readByte(ByteBuffer file) throws IOException {
		file.get(buff);
		return (buff[0]&0xff);
	}
	
	static byte[] buffer = new byte[4];
	public static int readInt(ByteBuffer file) throws IOException {
		file.get(buffer);
		return byteToInt(buffer);
	}
	
	public float readFloat(ByteBuffer file) throws IOException {
		file.get(buffer);
		return byteToFloat(buffer);
	}
	
	public short readShort(ByteBuffer file) throws IOException{
		file.get(buffer,0,2);
		return byteToShort(buffer);
	}
	
	public float byteToFloat(byte[]  buffer) {
		return Float.intBitsToFloat(byteToInt(buffer));
		//return (buffer[0]&0xff) |((buffer[1]&0xff)<<8) |((buffer[2]&0xff)<<16) |((buffer[3]&0xff)<<24);
	}
	
	public static int byteToInt(byte[]  buffer) {
		return (buffer[0]&0xff) |((buffer[1]&0xff)<<8) |((buffer[2]&0xff)<<16) |((buffer[3]&0xff)<<24);
	}
	
	public short byteToShort(byte[]  buffer) {
		return (short)((buffer[0]&0xff) |((buffer[1]&0xff)<<8));
	}

	public static char whichOf(char c, char[] check){
		for (int i=0;i<check.length;i++){
			if (c==check[i]) return check[i];
		}
		return '\u0000';
	}

	public static int indexOfAny(String str, char[] c, int skip){
		int i=0;
		for (i=i;i<str.length();i++){
			for (int j=0;j<c.length;j++){
				if (str.charAt(i) == c[j]) {
					if (skip >0){
						str.substring(i+1, str.length());
						i=0;
						skip--;
					}
					else return i;
				}
			}
			
		}

		return -1;
	}

	public static int indexOfAny(String str, char[] c){
		int i=0;
		for (i=i;i<str.length();i++){
			for (int j=0;j<c.length;j++){
				if (str.charAt(i) == c[j]) return i;
			}
			
		}

		return -1;
	}

	public static boolean stringCheck(byte[] f, int i, String str){
		byte[] arr = new byte[str.length()];
		if (i-str.length()>=0){
			for (int j=arr.length;j>0;j--){
				arr[arr.length-j] = f[i-j];
			}
			String check = new String(arr, StandardCharsets.UTF_8);
			return str.equals(check);
		}
		else return false;
	}

	public static boolean anyMatch(char[] c, byte open){
		for (int i=0;i<c.length;i++){
			if ((byte)c[i] == open) return true;
		}
		return false;
	}

	public static Integer toInteger(Byte[] bytes){
		Integer value = 0;
		for (byte b : bytes) {
			value = (value << 8) + (b & 0xFF);
		}
		return value;
	}

	public static Object[] readFromUntil(byte[] f, int i, char[] c, char open, char close) throws IOException{
		int scope = 0;
		List<Byte> s = new LinkedList<Byte>();
		byte[] g = new byte[c.length];
		for (int j=0;j<g.length;j++){
			g[j] = f[i+j];
		}

		for (i=i;i<f.length;i++){
			if (f[i]==open) {
				scope++; 
				s.add(f[i]);
			}
			else if (anyMatch(c, f[i]) && scope==0) {
				//s.add(f[i]);
				break;
			}
			else if (anyMatch(c, f[i]) && scope>0 && f[i]==close){
				scope--;
				s.add(f[i]);
			}
			else if (i==f.length-1) {
				s.add(f[i]);
				break;
			}
			else s.add(f[i]);
		}

		byte[] d = list2bytes(s);
		String str = new String(d, "SHIFT-JIS");
		//System.out.println(str);
		Object[] out = {str, i};
		return out;
	}

	public static Object[] readFromUntil(byte[] f, int i, byte c, char open) throws IOException{
		int scope = 0;
		List<Byte> s = new LinkedList<Byte>();

		for (i=i;i<f.length;i++){
			if (f[i]==open) {
				scope++; 
				s.add(f[i]);
			}
			else if (f[i]==c && scope>0){
				scope--;
				s.add(f[i]);
			}
			else if (f[i]==c && scope==0) {
				//s.add(f[i]);
				break;
			}
			else s.add(f[i]);
		}

		byte[] d = list2bytes(s);
		String str = new String(d, "SHIFT-JIS");
		//System.out.println(str);
		Object[] out = {str, i};
		return out;
	}

	public static Object[] readFromUntil(byte[] f, int i, byte[] c) throws IOException{
		byte[] g = new byte[c.length];
		for (int j=0;j<g.length;j++){
			g[j] = f[i+j];
		}

		List<Byte> s = new LinkedList<Byte>();
		while (!Arrays.equals(g,c)){
			i++;
			for (int j=0;j<g.length;j++){
				g[j] = f[i+j];
			}
			s.add(f[i]);	
		}
		byte[] d = list2bytes(s);
		String str = new String(d, "SHIFT-JIS");
		Object[] out = {str,i};
		return out;
	}

	public static Object[] readFromUntil(byte[] f, int i, char c) throws IOException{
		return readFromUntil(f,i,(byte)c);
	}
	public static Object[] readFromUntil(byte[] f, int i, byte c) throws IOException{
		
		List<Byte> s = new LinkedList<Byte>();
		while (f[++i] != c){
			s.add( f[i]);
		}
		byte[] d = list2bytes(s);
		String str = new String(d, "SHIFT-JIS");
		//System.out.println(str);
		Object[] out = {str, i};
		return out;
	}

	public static byte[] ByteArr2byteArr(Byte[] arr) {
		byte[] bytes = new byte[arr.length];
		int i=0;
		for (Byte b: arr){
			bytes[i++] = b.byteValue();
		}
		return bytes;
	}
	public static Byte[] byteArr2ByteArr(byte[] arr) {
		Byte[] bytes = new Byte[arr.length];
		int i=0;
		for (byte b: arr){
			bytes[i++] = b;
		}
		return bytes;
	}

	public static final byte[] intToByteArray(int value) {
		return new byte[] {
				(byte)(value >>> 24),
				(byte)(value >>> 16),
				(byte)(value >>> 8),
				(byte)value};
	}

	public static byte[] list2bytes(List<Byte> c) throws IOException {
		byte[] d = new byte[c.size()];
		/*for (int i=0;i<c.size();i++){
			d[i] = c.get(i);
		}*/
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		for (byte element : c){
			out.write(element);
		}
		d = baos.toByteArray();
		return d;
	}

	public static int createFile(String path, String filename) {
		  try {
			File myObj = new File(filename+".txt");
			if (myObj.createNewFile()) {
			  System.out.println("File created: " + myObj.getName());
			  return 0;//Pass
			} else {
			  System.out.println("File already exists.");
			  return 1;//Fail, already exists
			}
		  } catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
			return 2;//Fail, other error
		  }
	  }

	  public static String[] readLines(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines.toArray(new String[lines.size()]);
    }

	public static ByteBuffer readFile(String filepath) throws IOException{
		RandomAccessFile aFile = new RandomAccessFile(filepath, "r");
		//System.out.println("\n"+filepath+" found.");
		FileChannel inChannel = aFile.getChannel();
		long fileSize = inChannel.size();
		ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
		inChannel.read(buffer);
		buffer.flip();
		return buffer;
	}

	//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		//
		// Handle args
		//
		List arg = Arrays.asList(args);
		// No args, or too few args
		if (arg.size() < 3 /*|| !arg.contains("-h")*/){
			String blah = "Usage: java -jar PATcopier.jar"+
			" receivingPAT sourcePAT outputPAT startno endno"+
			"\n\tstartno is the first pattern to copy; endno is the last."+
			"\n\tenter as many pairs of startno and endno ranges as you want."+
			"\n\tadd -k to keep pattern IDs the same, if possible.";
			System.out.println(blah);
			return;
		}
		// List copy patterns
		List<Integer> copyPatterns = new ArrayList<Integer>();
		int ranges = (args.length-3)/2;
		int[] start=new int[ranges], end=new int[ranges];

			//try {
				for (int i=0;i<ranges+0;i++){
					int x = Integer.parseInt(args[3+((i+1)*2)-2]);
					int y = Integer.parseInt(args[3+((i+1)*2)-1]);
					start[i]=x;
					end[i]=y;
					for (int j=0;j<end[i]-start[i];j++){
						copyPatterns.add(start[i]+j);
					}
					copyPatterns.add(end[i]);
				}
			//} catch (NumberFormatException e) { System.out.println("\nOne of your pattern number args is not an integer value. Closing.");return;}
		// Required args
		String inPATpath = args[0].replace('\\','/');
		String sourcePATpath = args[1].replace('\\','/'); 
		String outPATpath = args[2].replace('\\','/');
		ByteBuffer inPAT = readFile(inPATpath);
		ByteBuffer sourcePAT = readFile(inPATpath);
		boolean keepIDs = args[args.length-1].matches("-k") ? true : false;

		//
		// Get pattern bytes from source PAT
		//
		TreeMap<Integer,Byte[]> patterns = new TreeMap<Integer,Byte[]>();
		for (int i=0;i<copyPatterns.size();i++){
			sourcePAT = readFile(sourcePATpath);
			patterns.put(copyPatterns.get(i), getByteRange(sourcePAT,copyPatterns.get(i).intValue(),"P_ST","P_ED"));
			if (patterns.get(copyPatterns.get(i))==null) {
				System.out.println(copyPatterns.get(i)+" doesn't exist in source PAT.");
				patterns.remove(copyPatterns.get(i));
				//return; // This is too common across ranges to break on.
			}
			else System.out.println(copyPatterns.get(i)+" (pattern) stored.");
		}

		//
		// Get all used part IDs
		//
		TreeMap<Integer,Integer[]> patternParts = new TreeMap<Integer,Integer[]>();
		List<Integer> copyParts = new ArrayList<Integer>();
		for (HashMap.Entry<Integer,Byte[]> entry : patterns.entrySet()){
			Integer[] ids = getIDs(entry.getValue(),"PRID");
			
			if (ids==null){
				System.out.println(entry.getKey()+" lacks a PRID.");
			}
			else {
				for (int i=0;i<ids.length;i++){
					copyParts.add(ids[i]);
				}
				patternParts.put(entry.getKey(), copyParts.toArray(Integer[]::new));
			}
		}

		//
		// Get part bytes from source PAT
		//
		TreeMap<Integer,Byte[]> parts = new TreeMap<Integer,Byte[]>();
		for (HashMap.Entry<Integer,Integer[]> entry : patternParts.entrySet()){
			for (int i=0;i<entry.getValue().length;i++){
				if (!parts.keySet().contains(entry.getValue()[i])){
					sourcePAT = readFile(sourcePATpath);
					Byte[] part = getByteRange(sourcePAT, entry.getValue()[i], "PPST", "PPED");
					parts.put(entry.getValue()[i], part);
					System.out.println(entry.getValue()[i]+" (part) stored.");
				}
				//else System.out.println(entry.getKey()+" already exists in parts map.");
			}
		}

		//
		// Get all used texture IDs
		//
		TreeMap<Integer,Integer[]> partTextures = new TreeMap<Integer,Integer[]>();
		List<Integer> copyTextures = new ArrayList<Integer>();
		for (HashMap.Entry<Integer,Byte[]> entry : parts.entrySet()){
			Integer[] ids = getIDs(entry.getValue(),"PPTP");
			
			if (ids==null){
				System.out.println(entry.getKey()+" lacks a PPTP.");
			}
			else {
				for (int i=0;i<ids.length;i++){
					copyTextures.add(ids[i]);
				}
				partTextures.put(entry.getKey(), copyTextures.toArray(Integer[]::new));
			}
		}

		//
		// Get texture bytes from source PAT
		//
		TreeMap<Integer,Byte[]> textures = new TreeMap<Integer,Byte[]>();
		for (HashMap.Entry<Integer,Integer[]> entry : partTextures.entrySet()){
			for (int i=0;i<entry.getValue().length;i++){
				if (!textures.keySet().contains(entry.getValue()[i])){
					sourcePAT = readFile(sourcePATpath);
					Byte[] texture = getByteRange(sourcePAT, entry.getValue()[i], "PGST", "PGED");
					textures.put(entry.getValue()[i], texture);
					System.out.println(entry.getValue()[i]+" (texture) stored.");
				}
			}
		}

		//
		// Get all used shape IDs
		//
		TreeMap<Integer,Integer[]> partShapes = new TreeMap<Integer,Integer[]>();
		List<Integer> copyShapes = new ArrayList<Integer>();
		for (HashMap.Entry<Integer,Byte[]> entry : parts.entrySet()){
			Integer id = getID(entry.getValue(),"PPPP");
			
			if (id==null){
				System.out.println(entry.getKey()+" lacks a PPPP. Defaulting to shape 0.");
				id = 0;
			}
			copyShapes.add(id);
			partShapes.put(entry.getKey(), copyShapes.toArray(Integer[]::new));
			//System.out.println(id);
			
		}

		//
		// Get shape bytes from source PAT. No IDs defined, so goes by count.
		//
		TreeMap<Integer,Byte[]> shapes = new TreeMap<Integer,Byte[]>();
		Integer sourceShapeCount = null;
		for (HashMap.Entry<Integer,Integer[]> entry : partShapes.entrySet()){
			for (int i=0;i<entry.getValue().length;i++){
				if (!shapes.keySet().contains(entry.getValue()[i])){
					sourcePAT = readFile(sourcePATpath);
					byte[] temp = new byte[sourcePAT.remaining()];
					sourcePAT.get(temp);
					sourceShapeCount = getID(byteArr2ByteArr(temp), "VEST");
					sourcePAT = readFile(sourcePATpath);
					Byte[] shape = getByteRange(sourcePAT, entry.getValue()[i], "VEST", 64); //TODO: Length of 64 is an assumption but seems consistent
					shapes.put(entry.getValue()[i], shape);
					System.out.println(entry.getValue()[i]+" (shape) stored.");
				}
				//else System.out.println(entry.getKey()+" already exists in parts map.");
			}
		}
		//
		// Get shapename bytes from source PAT. No IDs defined, so goes by count.
		//
		TreeMap<Integer,Byte[]> shapenames = new TreeMap<Integer,Byte[]>();
		for (HashMap.Entry<Integer,Integer[]> entry : partShapes.entrySet()){
			for (int i=0;i<entry.getValue().length;i++){
				if (!shapenames.keySet().contains(entry.getValue()[i])){
					sourcePAT = readFile(sourcePATpath);
					Byte[] shapename = getByteRange(sourcePAT, entry.getValue()[i], "VNST", 32); //TODO: Length of 32 is an assumption but seems consistent
					shapenames.put(entry.getValue()[i], shapename);
					System.out.println(entry.getValue()[i]+" (shapename) stored.");
				}
				//else System.out.println(entry.getKey()+" already exists in parts map.");
			}
		}

		System.out.println("\nAll objects stored.\n\nRemaps:");

		//
		// Remap new shape IDs (aka append to inPAT shapelist)
		//
		TreeMap<Integer,Integer> shapesMap = new TreeMap<Integer,Integer>();
		Integer inShapeCount = null;
		inPAT = readFile(inPATpath);
		byte[] t = new byte[inPAT.remaining()];
		inPAT.get(t);
		inShapeCount = getID(byteArr2ByteArr(t), "VEST");
		//shapes.remove(0); // 0 is a default shape that's identical across games. No need to copy.
		//shapenames.remove(0);
		//System.out.println("Skipping default shape 0.");
		for (HashMap.Entry<Integer,Byte[]> entry : shapes.entrySet()){
			shapesMap.put(entry.getKey(),++inShapeCount);
			System.out.println("Shape "+entry.getKey()+" -> "+inShapeCount);
		}
		// Note: inShapeCount is now updated with final shape count after copy
		
		//
		// Remap new texture IDs
		//
		TreeMap<Integer,Integer> texturesMap = new TreeMap<Integer,Integer>();
		TreeMap<Integer,Byte[]> texBackup = (TreeMap<Integer, Byte[]>) textures.clone();
		inPAT = readFile(inPATpath);
		Integer[] usedTextureIDs = getIDs(inPAT,"PGST");
		List<Integer> availableTextureIDs = new ArrayList<Integer>();
		for (int i=0;i<99;i++){
			availableTextureIDs.add(i);
		}
		for (int i=0;i<usedTextureIDs.length;i++){
			if (availableTextureIDs.contains(usedTextureIDs[i])) availableTextureIDs.remove(usedTextureIDs[i]);
		}
		for (HashMap.Entry<Integer,Byte[]> entry : textures.entrySet()){
			if (entry.getValue() != null){
				texturesMap.put(entry.getKey(), availableTextureIDs.get(0));
				System.out.println("Texture "+entry.getKey()+" -> "+availableTextureIDs.get(0));
				availableTextureIDs.remove(0);
			}
			else {
				texBackup.remove(entry.getKey()); // PPTPs that point to nothing don't render...
				System.out.println("Texture "+entry.getKey()+" doesn't actually exist in source pat. Removing.");
				texturesMap.put(entry.getKey(), -1);
				//System.out.println("Texture "+entry.getKey()+" -> "+32767);
			}
		}
		textures = (TreeMap<Integer, Byte[]>) texBackup.clone();
		
		//
		// Remap new part IDs
		//
		TreeMap<Integer,Integer> partsMap = new TreeMap<Integer,Integer>();
		inPAT = readFile(inPATpath);
		Integer[] usedPartIDs = getIDs(inPAT,"PPST");
		List<Integer> availablePartIDs = new ArrayList<Integer>();
		for (int i=0;i<1500;i++){
			availablePartIDs.add(i);
		}
		for (int i=0;i<usedPartIDs.length;i++){
			if (availablePartIDs.contains(usedPartIDs[i])) availablePartIDs.remove(usedPartIDs[i]);
		}
		for (HashMap.Entry<Integer,Byte[]> entry : parts.entrySet()){
			partsMap.put(entry.getKey(), availablePartIDs.get(0));
			System.out.println("Part "+entry.getKey()+" -> "+availablePartIDs.get(0));
			availablePartIDs.remove(0);
		}
		// availablePartIDs now lists only IDs that are unused even after our copying.
		System.out.println("There are "+availablePartIDs.size()+" unused part IDs left in this PAT.");

		//
		// Remap new pattern IDs
		//
		TreeMap<Integer,Integer> patternsMap = new TreeMap<Integer,Integer>();
		inPAT = readFile(inPATpath);
		Integer[] usedPatternIDs = getIDs(inPAT,"P_ST");
		List<Integer> availablePatternIDs = new ArrayList<Integer>();
		keepIDsloop: if (keepIDs){
			for (int i=0;i<copyPatterns.size();i++){
				for (int j=0;j<usedPatternIDs.length;j++){
					if (copyPatterns.get(i) == usedPatternIDs[j]){
						System.out.println("Overlap was found between source pattern ID and in pattern IDs."+
						"\nPattern IDs must be reassigned, so overwriting -k request.");
						keepIDs = false;
						break keepIDsloop;
					} 
				}
			}
			// Survived the ID comparing!
			System.out.println("No overlap between source pattern IDs and in pattern IDs."+
			"\nAll pattern IDs will be left as-is.");
			for (HashMap.Entry<Integer,Byte[]> entry : patterns.entrySet()){
				patternsMap.put(entry.getKey(), entry.getKey());
			}
		}
		if (!keepIDs){
			for (int i=1;i<9999;i++){
				availablePatternIDs.add(i);
			}
			for (int i=0;i<usedPatternIDs.length;i++){
				if (availablePatternIDs.contains(usedPatternIDs[i])) availablePatternIDs.remove(usedPatternIDs[i]);
			}
			for (HashMap.Entry<Integer,Byte[]> entry : patterns.entrySet()){
				patternsMap.put(entry.getKey(), availablePatternIDs.get(0));
				System.out.println("Pattern "+entry.getKey()+" -> "+availablePatternIDs.get(0));
				availablePatternIDs.remove(0);
			}
		}
		// I don't think there's a limit on pattern IDs...

		//
		// Update PRIDs in patterns (part ID)
		//
		//temp map
		TreeMap<Integer,Byte[]> tempMap = (TreeMap<Integer, Byte[]>) patterns.clone();

		Byte[] tempArr = new Byte[0];
		int replacements = 0;
		for (HashMap.Entry<Integer,Byte[]> pattern : patterns.entrySet()){
			tempArr = pattern.getValue();
			for (HashMap.Entry<Integer,Integer> part : partsMap.entrySet()){
				tempArr = replaceIDs(tempArr,"PRID", part.getKey(), part.getValue(), "P_ED");
				replacements++;
			}
			patterns.put(pattern.getKey(), tempArr);
		}

		//
		// Update PPPP in parts (shape ID)
		//
		tempArr = new Byte[0];
		for (HashMap.Entry<Integer,Byte[]> part : parts.entrySet()){
			tempArr = part.getValue();
			for (HashMap.Entry<Integer,Integer> shape : shapesMap.entrySet()){
				tempArr = replaceIDs(tempArr,"PPPP", shape.getKey(), shape.getValue()-1, "PPED");
			}
			for (HashMap.Entry<Integer,Integer> texture : texturesMap.entrySet()){
				tempArr = replaceIDs(tempArr,"PPTP", texture.getKey(), texture.getValue(), "PPED");
			}
			parts.put(part.getKey(), tempArr);
		}

		//
		// Update PPTP in parts (texture ID)
		//
		/*tempArr = new Byte[0];
		for (HashMap.Entry<Integer,Byte[]> part : parts.entrySet()){
			tempArr = part.getValue();
			
			parts.put(part.getKey(), tempArr);
		}*/

		//
		// Append copied patterns to end of inPAT's pattern list
		//
		// Find index of last P_ED
		inPAT = readFile(inPATpath);
		int p_ed = lastIndexOf(inPAT,"P_ED")+1;
		inPAT = readFile(inPATpath);
		byte[] arr = new byte[inPAT.remaining()];
		inPAT.get(arr);
		// Create new running byte array (list) for final
		List<Byte> destList = new ArrayList<Byte>();
		for (byte b : Arrays.copyOfRange(arr, 0, p_ed)){
			destList.add(b);
		}
		// Append our new patterns
		for (HashMap.Entry<Integer,Byte[]> entry : patterns.entrySet()){
			// P_ST
			destList.add((byte)'P');destList.add((byte)'_');destList.add((byte)'S');destList.add((byte)'T');
			// remapped ID
			Byte[] temp = byteArr2ByteArr(intToByteArray(patternsMap.get(entry.getKey())));
			destList.add(temp[3]);destList.add(temp[2]);destList.add(temp[1]);destList.add(temp[0]);
			// pattern content
			for (int i=0;i<entry.getValue().length;i++) destList.add(entry.getValue()[i]);
			// P_ED // We now appropriately add this from source.
			//destList.add((byte)'P');destList.add((byte)'_');destList.add((byte)'E');destList.add((byte)'D');
		}

		//
		// Append copied parts to end of inPAT's part list
		//
		// Find index of last PPED
		inPAT = readFile(inPATpath);
		int pped = lastIndexOf(inPAT,"PPED")+1;
		inPAT = readFile(inPATpath);
		arr = new byte[inPAT.remaining()];
		inPAT.get(arr);
		//destList = new ArrayList<Byte>();
		for (byte b : Arrays.copyOfRange(arr, p_ed, pped)){
			destList.add(b);
		}
		// Append our new parts
		for (HashMap.Entry<Integer,Byte[]> entry : parts.entrySet()){
			// PPST
			destList.add((byte)'P');destList.add((byte)'P');destList.add((byte)'S');destList.add((byte)'T');
			// remapped ID
			Byte[] temp = byteArr2ByteArr(intToByteArray(partsMap.get(entry.getKey())));
			destList.add(temp[3]);destList.add(temp[2]);destList.add(temp[1]);destList.add(temp[0]);
			// part content
			for (int i=0;i<entry.getValue().length;i++) destList.add(entry.getValue()[i]);
			// PPED
			//destList.add((byte)'P');destList.add((byte)'P');destList.add((byte)'E');destList.add((byte)'D');
		}

		//
		// Append our new shapes
		//
		//First we need to correct the total shape count
		
		destList.add((byte)'V');destList.add((byte)'E');destList.add((byte)'S');destList.add((byte)'T');
		Byte[] temp = byteArr2ByteArr(intToByteArray(inShapeCount)); // Note that inShapeCount is updated to total shape count by now
		destList.add(temp[3]);destList.add(temp[2]);destList.add(temp[1]);destList.add(temp[0]);

		inPAT = readFile(inPATpath);
		int vestEnd = lastIndexOf(inPAT,"VNST")+1-4; // there's no VEED tag here, so...
		inPAT = readFile(inPATpath);
		arr = new byte[inPAT.remaining()];
		inPAT.get(arr);
		for (byte b : Arrays.copyOfRange(arr, pped+(4*2), vestEnd)) destList.add(b); // Skip VEST and the shape count
		//Write our shapes in
		for (HashMap.Entry<Integer,Byte[]> entry : shapes.entrySet()){
			// shapes content
			for (int i=0;i<entry.getValue().length;i++) {
				destList.add(entry.getValue()[i]);
			}
			
		}
		// VNST
		//destList.add((byte)'V');destList.add((byte)'N');destList.add((byte)'S');destList.add((byte)'T');
		
		//
		// Append copied shapenames to end of inPAT's shapename list
		//
		inPAT = readFile(inPATpath);
		int veed = lastIndexOf(inPAT,"VEED")+1-4; // there's no VNED tag, but the VEED tag is here. ???
		inPAT = readFile(inPATpath);
		arr = new byte[inPAT.remaining()];
		inPAT.get(arr);
		for (byte b : Arrays.copyOfRange(arr, vestEnd, veed)) destList.add(b); // Skip VEST and the shape count
		//Write our shapenames in
		for (HashMap.Entry<Integer,Byte[]> entry : shapenames.entrySet()){
			// shapenames content
			for (int i=0;i<entry.getValue().length;i++) destList.add(entry.getValue()[i]);
		}
		// VEED
		destList.add((byte)'V');destList.add((byte)'E');destList.add((byte)'E');destList.add((byte)'D');

		//
		// Append copied textures to end of inPAT's texture list
		//
		// Find index of last PGED
		inPAT = readFile(inPATpath);
		int pged = lastIndexOf(inPAT,"PGED")+1;
		inPAT = readFile(inPATpath);
		arr = new byte[inPAT.remaining()];
		inPAT.get(arr);
		//destList = new ArrayList<Byte>();
		inPAT = readFile(inPATpath);
		for (byte b : Arrays.copyOfRange(arr, lastIndexOf(inPAT, "VEED")+1, pged)){
			destList.add(b);
		}
		// Append our new textures
		for (HashMap.Entry<Integer,Byte[]> entry : textures.entrySet()){
			if (entry.getValue()!=null){
				// PGST
				destList.add((byte)'P');destList.add((byte)'G');destList.add((byte)'S');destList.add((byte)'T');
				// remapped ID
				temp = byteArr2ByteArr(intToByteArray(texturesMap.get(entry.getKey())));
				destList.add(temp[3]);destList.add(temp[2]);destList.add(temp[1]);destList.add(temp[0]);
				// texture content
				for (int i=0;i<entry.getValue().length;i++) destList.add(entry.getValue()[i]);
			}
			// PGED already stored... Unless it's null
			//else {destList.add((byte)'P');destList.add((byte)'G');destList.add((byte)'E');destList.add((byte)'D');}
		}
		
		//EOF
		destList.add((byte)'_');destList.add((byte)'E');destList.add((byte)'N');destList.add((byte)'D');

		System.out.println("PAT assembly complete.");

		// test file TODO: clean up
		try (FileOutputStream fos = new FileOutputStream(outPATpath)){
			Byte[] asdf = destList.stream().toArray(Byte[]::new);
			byte[] jkl = new byte[asdf.length];
			for (int i=0;i<jkl.length;i++) jkl[i]=asdf[i].byteValue();
			fos.write(jkl);
		}
		System.out.println(outPATpath.substring(outPATpath.lastIndexOf("/")+1, outPATpath.length())+" created. Closing.");
		return;
	}

	private static Byte[] replaceIDs(Byte[] pattern, String key, Integer find, Integer replace, String end) throws Exception {
		ByteBuffer file = ByteBuffer.wrap(ByteArr2byteArr(pattern));
		int hits = 0;
		byte[] buffer = { file.get(), file.get(),file.get(),file.get() };
		List<Byte> output = new ArrayList<Byte>();

		do {
			String buf = new String(buffer,StandardCharsets.UTF_8);
			if (buf.equals(key)){
				int index = readInt(file);

				if (index == find) {
					// Hit

					// Copy key
					buffer = key.getBytes(StandardCharsets.UTF_8);
					for (byte b : buffer) output.add(b);

					// Replace ID
					buffer = reverse(intToByteArray(replace));
					for (byte b : buffer) output.add(b);
					hits++;
					if (!file.hasRemaining()) {
						return output.toArray(Byte[]::new);
					}
					buffer[0]=file.get();buffer[1]=file.get();buffer[2]=file.get();buffer[3]=file.get();
				}
				else {
					//Right key, wrong ID. Move along
					// Copy key
					buffer = key.getBytes(StandardCharsets.UTF_8);
					for (byte b : buffer) output.add(b);
					// Copy ID
					for (byte b : reverse(intToByteArray(index))) output.add(b);
					buffer[0]=file.get();buffer[1]=file.get();buffer[2]=file.get();buffer[3]=file.get();
				}
			}
			else if (buf.equals(end)){
				buffer = end.getBytes(StandardCharsets.UTF_8);
				for (byte b : buffer) output.add(b);
				return output.toArray(Byte[]::new);
			}
			else {
				
				
				// Move buffer forward 1 byte
				output.add(buffer[0]);
				buffer[0]=buffer[1];buffer[1]=buffer[2];buffer[2]=buffer[3];buffer[3]=file.get();
				// Move buffer forward 4 bytes
				//for (byte b : buffer) output.add(b);
				//for (byte b : buffer) b = file.get();
				
			}
		} while (true);
	}

	private static int lastIndexOf(ByteBuffer inPAT, String string) {
		byte[] arr = new byte[inPAT.remaining()];
		inPAT.get(arr);

		byte[] buffer = new byte[4];
		for (int i=arr.length-1;i>4;i--){
			buffer[0]=arr[i-3];buffer[1]=arr[i-2];buffer[2]=arr[i-1];buffer[3]=arr[i-0];
			String x = new String(buffer,StandardCharsets.UTF_8);
			if (x.equals(string)) return i;
		}
		return -1;
	}

	private static Integer[] getIDs(ByteBuffer file, String key) throws IOException {
		byte[] buffer = { file.get(), file.get(),file.get(),file.get() };
		List<Integer> result = new ArrayList<Integer>();

		do {
			String buf = new String(buffer,StandardCharsets.UTF_8);
			if (buf.equals(key)){
				int index = readInt(file);
				// Hit
				result.add(index);
				file.get();file.get();file.get();file.get();
			}
			//Move buffer forward 1 byte
			else buffer[0]=buffer[1];buffer[1]=buffer[2];buffer[2]=buffer[3];buffer[3]=file.get();
		} while (file.hasRemaining());
		// Miss
		return result.toArray(Integer[]::new);
	}

	private static Integer getID(Byte[] pattern, String key) throws IOException {
		byte[] x = new byte[pattern.length];
		int i=0;
		for (Byte b: pattern){
			x[i++] = b.byteValue();
		}
		ByteBuffer file = ByteBuffer.wrap(x);
		byte[] buffer = { file.get(), file.get(),file.get(),file.get() };
		List<Integer> result = new ArrayList<Integer>();

		do {
			String buf = new String(buffer,StandardCharsets.UTF_8);
			if (buf.equals(key)){
				int index = readInt(file);
				// Hit
				return index;
			}
			//Move buffer forward 1 byte
			else buffer[0]=buffer[1];buffer[1]=buffer[2];buffer[2]=buffer[3];buffer[3]=file.get();
		} while (file.hasRemaining());
		// Miss
		return null;
	}

	private static Integer[] getIDs(Byte[] pattern, String key) throws IOException {
		byte[] x = new byte[pattern.length];
		int i=0;
		for (Byte b: pattern){
			x[i++] = b.byteValue();
		}
		ByteBuffer file = ByteBuffer.wrap(x);
		byte[] buffer = { file.get(), file.get(),file.get(),file.get() };
		List<Integer> result = new ArrayList<Integer>();

		do {
			String buf = new String(buffer,StandardCharsets.UTF_8);
			if (buf.equals(key)){
				int index = readInt(file);
				// Hit
				result.add(index);
			}
			//Move buffer forward 1 byte
			else buffer[0]=buffer[1];buffer[1]=buffer[2];buffer[2]=buffer[3];buffer[3]=file.get();
		} while (file.hasRemaining());
		// Miss
		return result.toArray(Integer[]::new);
	}

	private static Byte[] getByteRange(ByteBuffer file, int integer, String key, String end) throws IOException {
		try {byte[] buffer = { file.get(), file.get(),file.get(),file.get() };}
		catch (Exception e){
			System.out.println("ID "+integer+" is too small to search."); return null;
		}

		do {
			String buf = new String(buffer,StandardCharsets.UTF_8);
			if (buf.equals(key)){
				int index = readInt(file);

				if (index == integer) {
					// Hit
					buffer[0]=file.get();buffer[1]=file.get();buffer[2]=file.get();buffer[3]=file.get();
					List<Byte> bytes = new ArrayList<Byte>();
					buf = new String(buffer,StandardCharsets.UTF_8);
					while (!buf.equals(end)) {
						buf = new String(buffer,StandardCharsets.UTF_8);
						bytes.add((Byte) buffer[0]);
						buffer[0]=buffer[1];buffer[1]=buffer[2];buffer[2]=buffer[3];buffer[3]=file.get();
					}
					bytes.remove(bytes.size()-1);
					buffer = end.getBytes(StandardCharsets.UTF_8);
					for (byte b : buffer) bytes.add(b);
					return bytes.toArray(Byte[]::new);
				}
			}
			//Move buffer forward 1 byte
			buffer[0]=buffer[1];buffer[1]=buffer[2];buffer[2]=buffer[3];buffer[3]=file.get();
		} while (file.hasRemaining());
		// Miss
		return null;
	}

	private static Byte[] getByteRange(ByteBuffer file, int integer, String key, int len) throws IOException {
		byte[] buffer = { file.get(), file.get(),file.get(),file.get() };
		

		do {
			String buf = new String(buffer,StandardCharsets.UTF_8);
			if (buf.equals(key)){
				int index;
				if (!key.equals("VNST")) index = readInt(file);
				if (key.equals("VEST")) index = readInt(file);

				// Hit
				for (int i=0;i<integer*len;i++) file.get(); // Offset to the correct shape
				//buffer[0]=file.get();buffer[1]=file.get();buffer[2]=file.get();buffer[3]=file.get();
				List<Byte> bytes = new ArrayList<Byte>();
				
				for (int i=0;i<len;i++) {
					bytes.add(file.get());
				}
				//bytes.remove(bytes.size()-1);
				buf = new String(buffer,StandardCharsets.UTF_8);
				return bytes.toArray(Byte[]::new);
				
			}
			//Move buffer forward 1 byte
			buffer[0]=buffer[1];buffer[1]=buffer[2];buffer[2]=buffer[3];buffer[3]=file.get();
		} while (file.hasRemaining());
		// Miss
		return null;
	}

	public static boolean nullSafeEquals(Object object1, Object object2) {
		if (object1 == object2) {
			return true;
		}
		if ((object1 == null && object2 != null) || (object1 != null && object2 == null)) {
			return false;
		}
		return object1.equals(object2);
	}

	public static byte[] reverse(byte[] buffer){
		byte[] t = new byte[4];
		t[3]=buffer[0];t[2]=buffer[1];t[1]=buffer[2];t[0]=buffer[3];
		return t;
	}
	public static Byte[] reverse(Byte[] buffer){
		Byte[] t = new Byte[4];
		t[3]=buffer[0];t[2]=buffer[1];t[1]=buffer[2];t[0]=buffer[3];
		return t;
	}

}
