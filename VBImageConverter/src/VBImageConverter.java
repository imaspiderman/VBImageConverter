import java.awt.image.BufferedImage;
import java.io.IOException;


public class VBImageConverter {
	java.awt.image.BufferedImage img = null;
	java.util.ArrayList<VBChar>chars;

	public static void main(String[] args) {
		if(args.length != 1){
			Usage();
			return;
		}
		VBImageConverter c = new VBImageConverter();
		c.setImage(args[0]);
		c.scaleImage();
		c.parseImage();
		c.writeImage(args[0]);
	}
	
	private static void Usage(){
		System.out.println("The image used must have a width and height divisible by 8.");
		System.out.println("The maximum size is 512x512. This program will automatically");
		System.out.println("scale the image if it is larger than the max or if the");
		System.out.println("width or height is not divisible by 8.");
		System.out.println();
		System.out.println("Usage: java VBImageConverter <filename>");
	}
	
	public void writeImage(String sPath){
		try{
			java.io.File f = new java.io.File(sPath + "_converted");
			javax.imageio.ImageIO.write(img, "png", f);
			this.chars = new java.util.ArrayList<VBChar>();
			for(int y=0; y<img.getHeight();y+=8){
				for(int x=0; x<img.getWidth(); x+=8){
					VBChar v = new VBChar(img.getSubimage(x, y, 8, 8));
					boolean bMatch = false;
					for(int i=0; i<this.chars.size(); i++){
						if(this.chars.get(i).Compare(v)){
							bMatch = true;
							break;
						}
					}
					if(!bMatch) this.chars.add(v);					
				}
			}
			System.out.printf("There are %d unique chars\n", this.chars.size());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void setImage(String sPath){
		try {
			img = javax.imageio.ImageIO.read(new java.io.File(sPath));
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	public void scaleImage(){
		int newWidth,newHeight;
		newWidth = ((BufferedImage)img).getWidth();
		newHeight = ((BufferedImage)img).getHeight();
		
		if(newWidth>512){
			newHeight = (int)(((double)newHeight/(double)newWidth) * (double)512);
			newWidth = 512;
			
		}
		if(newHeight>512){
			newWidth = (int)(((double)newWidth/(double)newHeight) * (double)512);
			newHeight = 512;
		}
		//make sure we're divisible by 8
		while((newWidth % 8)!=0) newWidth--;
		while((newHeight % 8)!=0) newHeight--;
		java.awt.Image i = img.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);
		BufferedImage b = new BufferedImage(newWidth,newHeight,BufferedImage.TYPE_INT_RGB);
		b.getGraphics().drawImage(i,0,0,null);
		img = b;
	}
	
	public void parseImage(){
		int[] iColor = new int[4];
		//Change to red image
		for(int x=0; x<img.getWidth(); x++){
			for(int y=0; y<img.getHeight(); y++){
				img.getRaster().getPixel(x, y, iColor);				
				int r = (int)(((double)(iColor[0] + iColor[1] + iColor[2]) / (double)765) * 255);

				int[] rc = new int[4];
				rc[0] = 0;
				rc[1] = 75;
				rc[2] = 125;
				rc[3] = 175;
				for(int i=0; i < 3; i++){
					if(r>rc[i] && r<=rc[i+1]){
						if(java.lang.Math.abs(r-rc[i+1]) > java.lang.Math.abs(r-rc[i])){
							r = rc[i];
						}else{
							r = rc[i+1];
						}
					}
				}
				img.getRaster().setPixel(x, y, new int[] {r,0,0,255});
			}
		}
	}
	
	public class VBChar {
		int[] iData;
		int[] c;
		public VBChar(java.awt.image.BufferedImage r){
			c = new int[3];
			iData = new int[4];
			
			int iCounter = 1;
			int idx = 0;
			for(int y=0; y<8; y++){
				for(int x=0; x<8; x++){
					r.getData().getPixel(x, y, c);
					iData[idx]=setVBPixel(iData[idx],c[0]);
					if(iCounter%16==0)idx++;
				}
			}
		}
		
		public int setVBPixel(int i, int c){
			int r = i;
			switch(c){
				case 0: r = (r<<2)|0; break;
				case 75: r = (r<<2)|1; break;
				case 125: r = (r<<2)|2; break;
				case 175: r = (r<<2)|3; break;
			}		
			return r;
		}
		
		public int[] getData(){
			return this.iData;
		}
		
		public boolean Compare(VBChar c){
			
			boolean comp = true;
			for(int i=0; i<4; i++){
				if(this.iData[i] != c.getData()[i]){
					comp = false;
					break;
				}
			}
			return comp;
		}
	}
}
