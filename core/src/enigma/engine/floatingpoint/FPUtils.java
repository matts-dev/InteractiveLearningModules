package enigma.engine.floatingpoint;

public class FPUtils
{
	static String getWholeNumberPortionString(float numberToParse)
	{
		Integer wholeNumber = (int) Math.floor(Math.abs(numberToParse));
		return wholeNumber.toString();
	}
	
	static String getFractionalPortionString(float numberToParse)
	{
		Integer wholeNumber = (int) Math.floor(Math.abs(numberToParse));
		Float fractionalPortion = (float) (Math.abs(numberToParse) - wholeNumber);
		String fracStr = fractionalPortion.toString();
		fracStr = fracStr.substring(0, fracStr.length() > 6 ? 6 : fracStr.length());
		
		//remove trailing zeros (if any)
		int lastDigitPointer = -1;
		for(int i = fracStr.length() - 1; i >= 0; --i)
		{
			if(fracStr.charAt(i) != '0')
			{
				lastDigitPointer = i;
				break;
			}
		}
		fracStr = fracStr.substring(0, lastDigitPointer + 1);
	
		return fracStr;
	}
}
