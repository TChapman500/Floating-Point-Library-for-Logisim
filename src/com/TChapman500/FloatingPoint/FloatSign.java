package com.TChapman500.FloatingPoint;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;

public class FloatSign extends InstanceFactory
{
	static final int PORT_IN0 = 0;
	static final int PORT_MODE = 1;
	static final int PORT_OUT0 = 2;
	static final int PORT_IN1 = 3;
	static final int PORT_OUT1 = 4;

	public FloatSign()
	{
		super("FP Sign Changer");
		setAttributes(new Attribute[] { FloatingPoint.PRECISION }, new Object[] { FloatingPoint.PRECISION.parse("single") });
		setIconName("negator.gif");
	}

	@Override
	public void paintInstance(InstancePainter painter)
	{
		// TODO Auto-generated method stub

		Graphics g = painter.getGraphics();
		painter.drawBounds();
		
		g.setColor(Color.GRAY);
		AttributeOption precision = painter.getAttributeValue(FloatingPoint.PRECISION);
		if (precision == FloatingPoint.SINGLE)
		{
			painter.drawPort(PORT_IN0);
			painter.drawPort(PORT_MODE, "abs", Direction.NORTH);
			painter.drawPort(PORT_OUT0);
		}
		else if (precision == FloatingPoint.DOUBLE)
		{
			painter.drawPort(PORT_IN0);
			painter.drawPort(PORT_IN1);
			painter.drawPort(PORT_MODE, "abs", Direction.NORTH);
			painter.drawPort(PORT_OUT0);
			painter.drawPort(PORT_OUT1);
		}
		g.setColor(Color.BLACK);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs)
	{
		AttributeOption precision = attrs.getValue(FloatingPoint.PRECISION);

		Bounds result;
		if (precision == FloatingPoint.DOUBLE) result = Bounds.create(-40, -20, 40, 50);
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
	
	private void UpdatePorts(Instance instance)
	{
		AttributeOption precision = instance.getAttributeValue(FloatingPoint.PRECISION);
		if (precision == FloatingPoint.SINGLE)
		{
			Port[] ports = new Port[3];
			ports[PORT_IN0] = new Port(-40, 0, Port.INPUT, 32);
			ports[PORT_MODE] = new Port(-20, -20, Port.INPUT, 1);
			ports[PORT_OUT0] = new Port(0, 0, Port.OUTPUT, 32);
			instance.setPorts(ports);
		}
		else if (precision == FloatingPoint.DOUBLE)
		{
			Port[] ports = new Port[5];
			ports[PORT_IN0] = new Port(-40, 0, Port.INPUT, 32);
			ports[PORT_IN1] = new Port(-40, 10, Port.INPUT, 32);
			ports[PORT_MODE] = new Port(-20, -20, Port.INPUT, 1);
			ports[PORT_OUT0] = new Port(0, 0, Port.OUTPUT, 32);
			ports[PORT_OUT1] = new Port(0, 10, Port.OUTPUT, 32);
			instance.setPorts(ports);
		}
	}
	
	@Override
	public void propagate(InstanceState state)
	{
		AttributeOption precision = state.getAttributeValue(FloatingPoint.PRECISION);
		if (precision == FloatingPoint.SINGLE)
		{
			Value a = state.getPortValue(PORT_IN0);
			Value mode = state.getPortValue(PORT_MODE);
			Value sum;
			
			if (a.isFullyDefined())
			{
				float aFloat = Float.intBitsToFloat(a.toIntValue());
				float sumFloat;
				
				// Adds by default, but subtracts if sub value is fully-defined.
				if (mode.isFullyDefined() && mode == Value.TRUE) sumFloat = Math.abs(aFloat);
				else sumFloat = -aFloat;
				
				// Result of the operation
				sum = Value.createKnown(BitWidth.create(32), Float.floatToIntBits(sumFloat));
			}
			else sum = Value.ERROR;
			
			state.setPort(PORT_OUT0, sum, 24);
		}
		else if (precision == FloatingPoint.DOUBLE)
		{
			Value a0 = state.getPortValue(PORT_IN0);
			Value a1 = state.getPortValue(PORT_IN1);
			Value mode = state.getPortValue(PORT_MODE);
			Value sum0;
			Value sum1;
			
			if (a0.isFullyDefined() && a1.isFullyDefined())
			{
				double aDouble = Double.longBitsToDouble(((long)a0.toIntValue() & 0x00000000FFFFFFFFL) | ((long)a1.toIntValue() << 32));
				double sumDouble;

				// Adds by default, but subtracts if sub value is fully-defined.
				if (mode.isFullyDefined() && mode == Value.TRUE) sumDouble = Math.abs(aDouble);
				else sumDouble = -aDouble;
				
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
