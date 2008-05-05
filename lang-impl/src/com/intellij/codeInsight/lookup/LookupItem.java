package com.intellij.codeInsight.lookup;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.simple.CompletionCharHandler;
import com.intellij.codeInsight.completion.simple.SimpleInsertHandler;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.util.containers.HashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an item of a lookup list.
 */
public class LookupItem<T> extends UserDataHolderBase implements Comparable, LookupElement<T>{
  public static final Object HIGHLIGHTED_ATTR = Key.create("highlighted");
  public static final Object TYPE_ATTR = Key.create("type");
  public static final Object ICON_ATTR = Key.create("icon");
  public static final Object TYPE_TEXT_ATTR = Key.create("typeText");
  public static final Object TAIL_TEXT_ATTR = Key.create("tailText");
  public static final Object TAIL_TEXT_SMALL_ATTR = Key.create("tailTextSmall");
  public static final Object FORCE_SHOW_SIGNATURE_ATTR = Key.create("forceShowSignature");
  public static final Key<Object> FORCE_SHOW_FQN_ATTR = Key.create("forseFQNForClasses");

  public static final Object DO_NOT_AUTOCOMPLETE_ATTR = Key.create("DO_NOT_AUTOCOMPLETE_ATTR");
  public static final Object DO_AUTOCOMPLETE_ATTR = Key.create("DO_AUTOCOMPLETE_ATTR");

  public static final Object GENERATE_ANONYMOUS_BODY_ATTR = Key.create("GENERATE_ANONYMOUS_BODY_ATTR");
  public static final Object BRACKETS_COUNT_ATTR = Key.create("BRACKETS_COUNT_ATTR");
  public static final Object OVERWRITE_ON_AUTOCOMPLETE_ATTR = Key.create("OVERWRITE_ON_AUTOCOMPLETE_ATTR");
  public static final Object NEW_OBJECT_ATTR = Key.create("NEW_OBJECT_ATTR");
  public static final Object DONT_CHECK_FOR_INNERS = Key.create("DONT_CHECK_FOR_INNERS");
  public static final Object FORCE_QUALIFY = Key.create("FORCE_QUALIFY");
  public static final Object SUBSTITUTOR = Key.create("SUBSTITUTOR");
  public static final Object TYPE = Key.create("TYPE");
  public static final Object INDICATE_ANONYMOUS = Key.create("INDICATE ANONYMOUS");
  public static final Key<String> INSERT_TYPE_PARAMS = Key.create("INSERT_TYPE_PARAMS");

  public static final Key<Comparable[]> WEIGHT = Key.create("WEIGHT");

  public static final Object CASE_INSENSITIVE = Key.create("CASE_INSENSITIVE");

  public static final Key<TailType> TAIL_TYPE_ATTR = Key.create("myTailType"); // one of constants defined in SimpleTailType interface

  private Object myObject;
  private String myLookupString;
  private InsertHandler myInsertHandler;
  private double myPriority;
  private int myGrouping;
  private Map<Object,Object> myAttributes = null;
  public static final LookupItem[] EMPTY_ARRAY = new LookupItem[0];
  @NotNull private CompletionCharHandler<T> myCompletionCharHandler = SimpleInsertHandler.DEFAULT_COMPLETION_CHAR_HANDLER;
  private final Set<String> myAllLookupStrings = new THashSet<String>();
  private String myPresentable;
  private AutoCompletionPolicy myAutoCompletionPolicy = AutoCompletionPolicy.SETTINGS_DEPENDENT;

  public LookupItem(T o, @NotNull @NonNls String lookupString){
    setObject(o);
    setLookupString(lookupString);
  }

  public void setObject(@NotNull T o) {
    myObject = o;

    if (o instanceof LookupValueWithPriority) {
      setPriority(((LookupValueWithPriority)o).getPriority());
    }
  }

  public boolean equals(Object o){
    if (o == this) return true;
    if (o instanceof LookupItem){
      LookupItem item = (LookupItem)o;
      return Comparing.equal(myObject, item.myObject)
             && Comparing.equal(myLookupString, item.myLookupString)
             && Comparing.equal(myAllLookupStrings, item.myAllLookupStrings)
             && Comparing.equal(myAttributes, item.myAttributes);
    }
    return false;
  }

  public int hashCode() {
    return myAllLookupStrings.hashCode() * 239 + getObject().hashCode();
  }

  public String toString() {
    return getLookupString();
  }

  /**
   * Returns a data object.  This object is used e.g. for rendering the node.
   */
  @NotNull
  public T getObject() {
    return (T)myObject;
  }

  /**
   * Returns a string which will be inserted to the editor when this item is
   * choosen.
   */
  @NotNull
  public String getLookupString() {
    return myLookupString;
  }

  public void setLookupString(@NotNull String lookupString) {
    if (myAllLookupStrings.contains("")) myAllLookupStrings.remove("");
    myLookupString = lookupString;
    myAllLookupStrings.add(lookupString);
  }

  public Object getAttribute(Object key){
    if (myAttributes != null){
      return myAttributes.get(key);
    }
    else{
      return null;
    }
  }

  public <T> T getAttribute(Key<T> key) {
    if (myAttributes != null){
      //noinspection unchecked
      return (T)myAttributes.get(key);
    }
    else{
      return null;
    }
  }

  public void setAttribute(Object key, Object value){
    if (myAttributes == null){
      myAttributes = new HashMap<Object, Object>(5);
    }
    myAttributes.put(key, value);
  }

  public <T> void setAttribute(Key<T> key, T value){
    if (myAttributes == null){
      myAttributes = new HashMap<Object, Object>(5);
    }
    myAttributes.put(key, value);
  }

  public InsertHandler getInsertHandler(){
    return myInsertHandler;
  }

  @NotNull
  public TailType getTailType(){
    final TailType tailType = getAttribute(TAIL_TYPE_ATTR);
    return tailType != null ? tailType : TailType.UNKNOWN;
  }

  @NotNull
  public LookupItem<T> setTailType(@NotNull TailType type) {
    setAttribute(TAIL_TYPE_ATTR, type);
    return this;
  }

  public int compareTo(Object o){
    if(o instanceof String){
      return getLookupString().compareTo((String)o);
    }
    if(!(o instanceof LookupItem)){
      throw new RuntimeException("Trying to compare LookupItem with " + o.getClass() + "!!!");
    }
    return getLookupString().compareTo(((LookupItem)o).getLookupString());
  }

  public LookupItem<T> setInsertHandler(@NotNull final InsertHandler handler) {
    myInsertHandler = handler;
    return this;
  }

  public LookupItem<T> setCompletionCharHandler(@NotNull final CompletionCharHandler<T> completionCharHandler) {
    myCompletionCharHandler = completionCharHandler;
    return this;
  }

  public LookupItem<T> setBold() {
    setAttribute(HIGHLIGHTED_ATTR, "");
    return this;
  }

  public LookupItem<T> setAutoCompletionPolicy(final AutoCompletionPolicy policy) {
    myAutoCompletionPolicy = policy;
    return this;
  }

  public AutoCompletionPolicy getAutoCompletionPolicy() {
    return myAutoCompletionPolicy;
  }

  @NotNull
  public LookupItem<T> setIcon(Icon icon) {
    setAttribute(ICON_ATTR, icon);
    return this;
  }

  @NotNull
  public LookupItem<T> setPriority(double priority) {
    myPriority = priority;
    return this;
  }

  @NotNull
  public LookupItem<T> setGrouping(final int grouping) {
    myGrouping = grouping;
    return this;
  }

  public final double getPriority() {
    return myPriority;
  }

  public final int getGrouping() {
    return myGrouping;
  }

  @NotNull
  public LookupItem<T> setPresentableText(@NotNull final String displayText) {
    myPresentable = displayText;
    return this;
  }

  @Nullable
  public String getPresentableText() {
    return myPresentable;
  }

  @NotNull
  public LookupItem<T> setTypeText(final String text) {
    setAttribute(TYPE_TEXT_ATTR, text);
    return this;
  }

  @NotNull
  public LookupItem<T> setCaseSensitive(final boolean caseSensitive) {
    setAttribute(CASE_INSENSITIVE, !caseSensitive);
    return this;
  }

  public LookupItem<T> addLookupStrings(@NonNls final String... additionalLookupStrings) {
    myAllLookupStrings.addAll(Arrays.asList(additionalLookupStrings));
    return this;
  }

  public Set<String> getAllLookupStrings() {
    return myAllLookupStrings;
  }

  public void copyAttributes(final LookupItem item) {
    if (myAttributes == null) {
      if (item.myAttributes == null) return;
      myAttributes = new HashMap<Object, Object>(5);
    }
    myAttributes.putAll(item.myAttributes);
  }

  @NotNull
  public CompletionCharHandler<T> getCompletionCharHandler() {
    return myCompletionCharHandler;
  }

}
