package net.manywiki.jee.actions;

import net.manywiki.jee.actions.other.DefaultAction;
import rebound.annotations.semantic.meta.dependencies.DependencyDirectory;

@DependencyDirectory("net/manywiki/jee/actions/pub")
public class ManyWikiActionBeans
{
	public static final String ActionBeansPrefix = "net.manywiki.jee.actions.pub.";
	public static final String ActionBeansSuffix = "";
	
	public static final Class<DefaultAction> DefaultActionBeanClass = DefaultAction.class;
}
