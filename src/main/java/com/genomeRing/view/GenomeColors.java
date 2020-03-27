package com.genomeRing.view;


import javafx.scene.paint.Color;

public class GenomeColors{

	protected Color[] colors;

	public GenomeColors() {
		colors  = colorBrewer( 9 );
	}

	public Color getColor(int index) {
		if (index >= colors.length) {
			// mix brewer+mayday
//			Color[] tmp = new Color[index+1];
//			int newcolors = index-8;
//			System.arraycopy(colors,0,tmp,0,9);
//			System.arraycopy(rainbow2(newcolors, 1),0,tmp,9,newcolors);
//			colors = tmp;

			// only mayday
			colors = rainbow2(index+1,1);
		}

		return colors[index];
	}

	public static Color alphaColor(Color c, int alpha) {
		if (c.getOpacity()==alpha)
			return c;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), (double)alpha/100);


	}

	/** create a color that looks like c painted with alpha transparency over a white background,
	 * but is in fact totally opaque */
	public static Color pseudoAlphaColor(Color c, int alpha) {
		double cPerc = (double)alpha/255.0;
		double whitePerc = 1 - cPerc;
		double whiteVal = whitePerc*255;
		return  Color.rgb((int)(c.getRed()*cPerc+whiteVal), (int)(c.getGreen()*cPerc+whiteVal), (int)(c.getBlue()*cPerc+whiteVal),1);
	}


	private Color[] colorBrewer(int n) {
		Color[] scheme = new Color[n];
		if(n >= 1) {
			scheme[0] = Color.rgb(228,26,28);
		}
		if(n >= 2) {
			scheme[1] = Color.rgb(55,126,184);
		}
		if(n >= 3) {
			scheme[2] = Color.rgb(77,175,74);
		}
		if(n >= 4) {
			scheme[3] = Color.rgb(152,78,163);
		}
		if(n >= 5) {
			scheme[4] = Color.rgb(255,127,0);
		}
		if(n >= 6) {
			scheme[5] = Color.rgb(255,255,51);
		}
		if(n >= 7) {
			scheme[6] = Color.rgb(166,86,40);
		}
		if(n >= 8) {
			scheme[7] = Color.rgb(247,129,191);
		}
		if(n >= 9) {
			scheme[8] = Color.rgb(153,153,153);
		}
		return scheme;
	}

	public static Color[] rainbow2(int colors, double saturation )
	{
		int groups = 6;

		if ( saturation > 1 )
			saturation = 1;

		if ( saturation < 0 )
			saturation = 0;

		int l_maxComponentValue =(int)(255 * saturation);

		int r = 255-l_maxComponentValue;
		int g = l_maxComponentValue;
		int b = l_maxComponentValue;
		int counter = 0;

		int maxC = groups * (l_maxComponentValue + 1);

		Color[] l_rainbow = new Color[maxC];


		for ( counter = 0; counter < maxC; ++counter )
		{
			if ( counter < 1 * l_maxComponentValue )
			{
				l_rainbow[counter] = Color.rgb( r, g, b--); // from cyan to green
			}
			if ( counter < 2 * l_maxComponentValue && counter >= 1 * l_maxComponentValue )
			{
				l_rainbow[counter] = Color.rgb( r++, g, b); // from green to yellow
			}
			if ( counter < 3 * l_maxComponentValue && counter >= 2 * l_maxComponentValue )
			{
				l_rainbow[counter] = Color.rgb( r, g--, b ); // from yellow to red
			}
			if ( counter < 4 * l_maxComponentValue && counter >= 3 * l_maxComponentValue )
			{
				l_rainbow[counter] = Color.rgb( r, g, b++ ); // from red to magenta
			}
			if ( counter < 5 * l_maxComponentValue && counter >= 4 * l_maxComponentValue )
			{
				l_rainbow[counter] = Color.rgb( r--, g, b ); // from magenta to blue
			}
			if ( counter < 6 * l_maxComponentValue && counter >= 5 * l_maxComponentValue )
			{
				l_rainbow[counter] = Color.rgb( r, g++, b ); // from blue to cyan
			}
		}

		Color[] l_result = new Color[colors];

		for ( int i = 0; i < colors; ++i )
		{
			double pos = ((double)maxC  / (double)colors ) * i;
			l_result[i] = l_rainbow[(int)pos];
		}

		return ( l_result );
	}


}
