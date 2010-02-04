/**
 * author: ali-erdem.ozcan@st.com
 */

package org.ow2.mind.idl.ast;

/**
 * AST node interface for <code>PrimitiveType</code> elements.
 */
public interface PrimitiveType extends Type {
  /** Enumeration of the primitive types. */
  public static enum PrimitiveTypeEnum {
    /** The name of the void primitive type. */
    VOID("void", "void"),

    /** The name of the boolean primitive type. */
    BOOLEAN("boolean", "boolean"),

    /** The name of the char primitive type. */
    CHAR("char", "char"),

    /** The name of the short primitive type. */
    SHORT("short", "short"),

    /** The name of the int primitive type. */
    INT("int", "int"),

    /** The name of the long primitive type. */
    LONG("long", "long"),

    /** The name of the float primitive type. */
    FLOAT("float", "float"),

    /** The name of the double primitive type. */
    DOUBLE("double", "double"),

    /** The name of the string primitive type. */
    STRING("string", "char*"),

    /** The name of the int8_t primitive type. */
    INT8_T("int8_t", "int8_t"),

    /** The name of the uint8_t primitive type. */
    UINT8_T("uint8_t", "uint8_t"),

    /** The name of the int16_t primitive type. */
    INT16_T("int16_t", "int16_t"),

    /** The name of the uint16_t primitive type. */
    UINT16_T("uint16_t", "uint16_t"),

    /** The name of the int32_t primitive type. */
    INT32_T("int32_t", "int32_t"),

    /** The name of the uint32_t primitive type. */
    UINT32_T("uint32_t", "uint32_t"),

    /** The name of the int64_t primitive type. */
    INT64_T("int64_t", "int64_t"),

    /** The name of the uint64_t primitive type. */
    UINT64_T("uint64_t", "uint64_t"),

    /** The name of the intptr_t primitive type. */
    INTPTR_T("intptr_t", "intptr_t"),

    /** The name of the uintptr_t primitive type. */
    UINTPTR_T("uintptr_t", "uintptr_t");

    private final String idlTypeName;
    private final String cType;

    PrimitiveTypeEnum(final String idlType, final String cType) {
      this.idlTypeName = idlType;
      this.cType = cType;
    }

    /**
     * @return the IDL representation of the primitive type.
     */
    public String getIdlTypeName() {
      return idlTypeName;
    }

    /**
     * @return the equivalent C type.
     */
    public String getCType() {
      return cType;
    }

    /**
     * Returns the enumeration constant that correspond to the given IDL type
     * name.
     * 
     * @param idlTypeName the name of the primitive type as it is found in the
     *          IDL.
     * @return the corresponding enumeration constant.
     * @throws IllegalArgumentException if the given IDL type name does not
     *           correspond to a primitive type.
     */
    public static PrimitiveTypeEnum fromIDLTypeName(final String idlTypeName) {
      for (final PrimitiveTypeEnum t : values()) {
        if (t.idlTypeName.equals(idlTypeName)) {
          return t;
        }
      }
      throw new IllegalArgumentException(
          "The given IDL type name does not correspond to a primitive type.");
    }

    public static boolean isPrimitive(final String idlTypeName) {
      for (final PrimitiveTypeEnum t : values()) {
        if (t.idlTypeName.equals(idlTypeName)) {
          return true;
        }
      }
      return false;
    }
  };

  /**
   * Returns the name of the primitive type.
   * 
   * @return the name of the primitive type.
   */
  String getName();

  /**
   * Set the name of the primitive type.
   * 
   * @param name the name of the primitive type to be set.
   */
  void setName(String name);
}
