package com.TChapman500.FloatingPoint;


import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.util.GraphicsUtil;

public class FloatAdd extends InstanceFactory
{
	private static final int PORT_A_IN0 = 0;
	private static final int PORT_B_IN0 = 1;
	private static final int PORT_OUT0 = 2;
	private static final int PORT_SUBTRACT = 3;
	
	private static final int PORT_A_IN1 = 4;
	private static final int PORT_B_IN1 = 5;
	private static final int PORT_OUT1 = 6;
	
	//private static final int PORT_A_IN2 = 7;
	//private static final int PORT_B_IN2 = 8;
	//private static final int PORT_OUT2 = 9;
	
	//private static final int PORT_A_IN3 = 10;
	//private static final int PORT_B_IN3 = 11;
	//private static final int PORT_OUT3 = 12;
	
	public FloatAdd()
	{
		// Set component name
		super("FP Adder");
		setAttributes(
			new Attribute[] { FloatingPoint.PRECISION },
			new AttributeOption[] { FloatingPoint.SINGLE }
		);
		
		// Set component appearance
		setIconName("adder.gif");
	}

	@Override
	public void paintInstance(InstancePainter painter)
	{
		Graphics g = painter.getGraphics();
		painter.drawBounds();
		
		// Draw ports for all precision selections.
		g.setColor(Color.GRAY);
		painter.drawPort(PORT_A_IN0);
		painter.drawPort(PORT_B_IN0);
		painter.drawPort(PORT_OUT0);
		painter.drawPort(PORT_SUBTRACT, "sub", Direction.NORTH);
		
		// Draw ports for double-precision and above
		AttributeOption precision = painter.getAttributeValue(FloatingPoint.PRECISION);
		if (precision == FloatingPoint.DOUBLE)
		{
			painter.drawPort(PORT_A_IN1);
			painter.drawPort(PORT_B_IN1);
			painter.drawPort(PORT_OUT1);
		}
		
		// Draw add symbol
		Location loc = painter.getLocation();
		int x = loc.getX();
		int y = loc.getY();
		GraphicsUtil.switchToWidth(g, 2);
		g.setColor(Color.BLACK);
		if (precision == FloatingPoint.SINGLE)
		{
			g.drawLine(x - 15, y, x - 5, y);
			g.drawLine(x - 10, y - 5, x - 10, y + 5);
		}
		else if (precision == FloatingPoint.DOUBLE)
		{
			g.drawLine(x - 15, y + 5, x - 5, y + 5);
			g.drawLine(x - 10, y, x - 10, y + 10);
		}
		GraphicsUtil.switchToWidth(g, 1);
	}
	
	private void UpdatePorts(Instance instance)
	{
		AttributeOption precision = instance.getAttributeValue(FloatingPoint.PRECISION);
		if (precision == FloatingPoint.SINGLE)
		{
			Port[] ports = new Port[4];
			ports[PORT_A_IN0] = new Port(-40, -10, Port.INPUT, 32);
			ports[PORT_B_IN0] = new Port(-40, 10, Port.INPUT, 32);
			ports[PORT_OUT0] = new Port(0, 0, Port.OUTPUT, 32);
			ports[PORT_SUBTRACT] = new Port(-20, -20, Port.INPUT, 1);
			instance.setPorts(ports);
		}
		else if (precision == FloatingPoint.DOUBLE)
		{
			Port[] ports = new Port[7];
			ports[PORT_A_IN0] = new Port(-40, -20, Port.INPUT, 32);
			ports[PORT_A_IN1] = new Port(-40, -10, Port.INPUT, 32);
			ports[PORT_B_IN0] = new Port(-40, 20, Port.INPUT, 32);
			ports[PORT_B_IN1] = new Port(-40, 30, Port.INPUT, 32);
			ports[PORT_OUT0] = new Port(0, 0, Port.OUTPUT, 32);
			ports[PORT_OUT1] = new Port(0, 10, Port.OUTPUT, 32);
			ports[PORT_SUBTRACT] = new Port(-20, -30, Port.INPUT, 1);
			instance.setPorts(ports);
		}
	}
	
	@Override
	public Bounds getOffsetBounds(AttributeSet attrs)
	{
		AttributeOption precision = attrs.getValue(FloatingPoint.PRECISION);

		Bounds result;
		if (precision == FloatingPoint.DOUBLE) result = Bounds.create(-40, -30, 40, 70);
		else result = Bounds.create(-40, -20, 40, 40);
		
		return result;
	}
	
	@Override
	public void instanceAttributeChanged(Instance instance, Attribute<?> attr)
	{
		instance.recomputeBounds();
		UpdatePorts(instance);
	}
	
	@Override
	public void configureNewInstance(Instance instance)
	{
		instance.addAttributeListener();
		UpdatePorts(instance);
	}
	
	@Override
	public void propagate(InstanceState state)
	{
		AttributeOption precision = state.getAttributeValue(FloatingPoint.PRECISION);
		
		if (precision == FloatingPoint.SINGLE)
		{
			// Get port values.
			Value a = state.getPortValue(PORT_A_IN0);
			Value b = state.getPortValue(PORT_B_IN0);
			Value sub = state.getPortValue(PORT_SUBTRACT);
			
			// Compute single-precision result.
			Value sum;
			if (a.isFullyDefined() && b.isFullyDefined())
			{
				float aFloat = Float.intBitsToFloat(a.toIntValue());
				float bFloat = Float.intBitsToFloat(b.toIntValue());
				float sumFloat;
				
				// Adds by default, but subtracts if sub value is fully-defined.
				if (sub.isFullyDefined() && sub == Value.TRUE) sumFloat = aFloat - bFloat;
				else sumFloat = aFloat + bFloat;
				
				// Result of the operation
				sum = Value.createKnown(BitWidth.create(32), Float.floatToIntBits(sumFloat));
			}
			else sum = Value.ERROR;
			
			// Update output port.
			state.setPort(PORT_OUT0, sum, 24);
		}
		else if (precision == FloatingPoint.DOUBLE)
		{
			Value a0 = state.getPortValue(PORT_A_IN0);
			Value a1 = state.getPortValue(PORT_A_IN1);
			Value b0 = state.getPortValue(PORT_B_IN0);
			Value b1 = state.getPortValue(PORT_B_IN1);
			Value sub = state.getPortValue(PORT_SUBTRACT);
			Value sum0;
			Value sum1;
			
			if (a0.isFullyDefined() && a1.isFullyDefined() && b0.isFullyDefined() && b1.isFullyDefined())
			{
				double aDouble = Double.longBitsToDouble(((long)a0.toIntValue() & 0x00000000FFFFFFFFL) | ((long)a1.toIntValue() << 32));
				double bDouble = Double.longBitsToDouble(((long)b0.toIntValue() & 0x00000000FFFFFFFFL) | ((long)b1.toIntValue() << 32));
				double sumDouble;
				
				if (sub.isFullyDefined() && sub == Value.TRUE) sumDouble = aDouble - bDouble;
				else sumDouble = aDouble + bDouble;
				
				long sumLong = Double.doubleToLongBits(sumDouble);
				int sum0Int = (int)sumLong;
				int sum1Int = (int)(sumLong >> 32);
				
				sum0 = Value.createKnown(BitWidth.create(32), sum0Int);
				sum1 = Value.createKnown(BitWidth.create(32), sum1Int);
			}
			else
			{
				sum0 = Value.ERROR;
				sum1 = Value.ERROR;
			}
			
			state.setPort(PORT_OUT0, sum0, 24);
			state.setPort(PORT_OUT1, sum1, 24);
		}
	}

}
