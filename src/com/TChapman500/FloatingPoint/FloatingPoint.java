package com.TChapman500.FloatingPoint;

import static com.cburch.logisim.std.Strings.S;

import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;

public class FloatingPoint extends Library
{
	List<AddTool> Tools;
	
	public FloatingPoint()
	{
		Tools = Arrays.asList(new AddTool[]
			{
				// Arithmatic Operations
				new AddTool(new FloatAdd()),		// Adds or Subtracts two numbers
				new AddTool(new FloatMultiply()),	// Multiplies two numbers
				new AddTool(new FloatDivide()),		// Divides two numbers, produces quotient and remainder
				//new AddTool(new FloatTrig()),		// Configurable to do Sin/Cos/Tan/ASin/ACos/ATan operations
				//new AddTool(new FloatSqrt()),		// Square root
				//new AddTool(new FloatCompare()),	// Compares two floating point numbers
				new AddTool(new FloatSign()),		// Negates or absolutes a number
				//new AddTool(new FloatRound()),	// Rounds number to selected integer
				
				// Conversion Operations
				//new AddTool(new FloatToInt()),	// Converts float to signed or unsigned integer
				//new AddTool(new IntToFloat()),	// Converts signed or unsigned integer to float
				//new AddTool(new FloatToDouble()),	// Converts float to double
				
				// Memory and I/O
				//new AddTool(new FloatRegister()),	// A single floating point register
				//new AddTool(new FloatRegisterBank()),	// A bank of floating point registers
				//new AddTool(new FloatProbe()),	// A floating point probe
				//new AddTool(new FloatPin()),		// A floating point probe
				//new AddTool(new FloatContant()),	// A floating point probe
			}
		);
	}
	
	public String getDisplayName()
	{
		return "Floating Point";
	}
	    
	/** Returns a list of all the tools available in this library. */
	@Override
	public List<AddTool> getTools()
	{
		return Tools;
	}
	
	public static final AttributeOption SINGLE = new AttributeOption("single", S.getter("Single"));
	public static final AttributeOption DOUBLE = new AttributeOption("double", S.getter("Double"));
	public static final AttributeOption EXTENDED = new AttributeOption("extended", S.getter("Extended"));
	public static final AttributeOption QUADRUPLE = new AttributeOption("quadruple", S.getter("Quadruple"));
	
	public static final Attribute<AttributeOption> PRECISION = Attributes.forOption("precision", S.getter("Precision"),
		new AttributeOption[]
		{ SINGLE, DOUBLE /*, EXTENDED, QUADRUPLE */ });
}
