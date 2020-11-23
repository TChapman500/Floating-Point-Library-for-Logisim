package com.TChapman500.FloatingPoint;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.util.GraphicsUtil;

public class FloatDivide extends InstanceFactory
{

	private static final int PORT_A_IN0 = 0;
	private static final int PORT_B_IN0 = 1;
	private static final int PORT_OUT0 = 2;
	private static final int PORT_A_IN1 = 3;
	private static final int PORT_B_IN1 = 4;
	private static final int PORT_OUT1 = 5;
	
	public FloatDivide()
	{
		super("FP Divider");
		setAttributes(
			new Attribute[] { FloatingPoint.PRECISION },
			new AttributeOption[] { FloatingPoint.SINGLE }
		);
		setIconName("divider.gif");
	}

	@Override
	public void paintInstance(InstancePainter painter)
	{
		// TODO Auto-generated method stub

		Graphics g = painter.getGraphics();
		painter.drawBounds();
		
		g.setColor(Color.GRAY);
		painter.drawPort(PORT_A_IN0);
		painter.drawPort(PORT_B_IN0);
		painter.drawPort(PORT_OUT0);

		// Draw ports for double-precision and above
		AttributeOption precision = painter.getAttributeValue(FloatingPoint.PRECISION);
		if (precision == FloatingPoint.DOUBLE)
		{
			painter.drawPort(PORT_A_IN1);
			painter.drawPort(PORT_B_IN1);
			painter.drawPort(PORT_OUT1);
		}
		
		Location loc = painter.getLocation();
		int x = loc.getX();
		int y = loc.getY();
		GraphicsUtil.switchToWidth(g, 2);
		g.setColor(Color.BLACK);
		if (precision == FloatingPoint.SINGLE)
		{
			g.fillOval(x - 12, y - 7, 4, 4);
			g.drawLine(x - 15, y, x - 5, y);
			g.fillOval(x - 12, y + 3, 4, 4);
		}
		else if (precision == FloatingPoint.DOUBLE)
		{
			g.fillOval(x - 12, y - 2, 4, 4);
			g.drawLine(x - 15, y + 5, x - 5, y + 5);
			g.fillOval(x - 12, y + 8, 4, 4);
		}
		GraphicsUtil.switchToWidth(g, 1);
	}

	private void UpdatePorts(Instance instance)
	{
		AttributeOption precision = instance.getAttributeValue(FloatingPoint.PRECISION);
		if (precision == FloatingPoint.SINGLE)
		{
			Port[] ports = new Port[3];
			ports[PORT_A_IN0] = new Port(-40, -10, Port.INPUT, 32);
			ports[PORT_B_IN0] = new Port(-40, 10, Port.INPUT, 32);
			ports[PORT_OUT0] = new Port(0, 0, Port.OUTPUT, 32);
			instance.setPorts(ports);
		}
		else if (precision == FloatingPoint.DOUBLE)
		{
			Port[] ports = new Port[6];
			ports[PORT_A_IN0] = new Port(-40, -20, Port.INPUT, 32);
			ports[PORT_A_IN1] = new Port(-40, -10, Port.INPUT, 32);
			ports[PORT_B_IN0] = new Port(-40, 20, Port.INPUT, 32);
			ports[PORT_B_IN1] = new Port(-40, 30, Port.INPUT, 32);
			ports[PORT_OUT0] = new Port(0, 0, Port.OUTPUT, 32);
			ports[PORT_OUT1] = new Port(0, 10, Port.OUTPUT, 32);
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
			Value a = state.getPortValue(PORT_A_IN0);
			Value b = state.getPortValue(PORT_B_IN0);
			Value product;

			if (a.isFullyDefined() && b.isFullyDefined())
			{
				float aFloat = Float.intBitsToFloat(a.toIntValue());
				float bFloat = Float.intBitsToFloat(b.toIntValue());
				float productFloat = aFloat / bFloat;
				product = Value.createKnown(BitWidth.create(32), Float.floatToIntBits(productFloat));
			}
			else product = Value.ERROR;
			
			state.setPort(PORT_OUT0, product, 16);
		}
		else if (precision == FloatingPoint.DOUBLE)
		{
			Value a0 = state.getPortValue(PORT_A_IN0);
			Value a1 = state.getPortValue(PORT_A_IN1);
			Value b0 = state.getPortValue(PORT_B_IN0);
			Value b1 = state.getPortValue(PORT_B_IN1);
			Value product0;
			Value product1;

			if (a0.isFullyDefined() && a1.isFullyDefined() && b0.isFullyDefined() && b1.isFullyDefined())
			{
				double aDouble = Double.longBitsToDouble(((long)a0.toIntValue() & 0x00000000FFFFFFFFL) | ((long)a1.toIntValue() << 32));
				double bDouble = Double.longBitsToDouble(((long)b0.toIntValue() & 0x00000000FFFFFFFFL) | ((long)b1.toIntValue() << 32));
				double productDouble;
				
				productDouble = aDouble / bDouble;
				
				long productLong = Double.doubleToLongBits(productDouble);
				int product0Int = (int)productLong;
				int product1Int = (int)(productLong >> 32);
				
				product0 = Value.createKnown(BitWidth.create(32), product0Int);
				product1 = Value.createKnown(BitWidth.create(32), product1Int);
			}
			else
			{
				product0 = Value.ERROR;
				product1 = Value.ERROR;
			}
			
			state.setPort(PORT_OUT0, product0, 16);
			state.setPort(PORT_OUT1, product1, 16);
		}
	}
}
