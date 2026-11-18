package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.util.AttributeSet;

/**
 * UikitCompatButton 是 UikitButton 的完美兼容别名。
 * 继承自 UikitButton，拥有所有原生代理方法，用于在 XML 中无缝平替原生 <Button>。
 */
public class UikitCompatButton extends UikitButton {
    public UikitCompatButton(Context context) { super(context); }
    public UikitCompatButton(Context context, AttributeSet attrs) { super(context, attrs); }
    public UikitCompatButton(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
}
