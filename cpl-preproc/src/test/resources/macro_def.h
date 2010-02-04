#ifndef MACRO_DEF_H
#define MACRO_DEF_H
#include <assert.h>


#ifndef COMPONENT_NAME
#error "Macro COMPONENT_NAME must be defined"
#endif

#define COMP_DATA struct { int a; } META_DATA
#define COMP_DATA_INIT { 1 }
#define COMP_DESC __component_##compName##_desc
#define CHECK_CONTEXT_PTR (assert (_mind_this != (void*) 0));
#define CONTEXT_PTR_DECL PRIVATE_DATA_T * _mind_this
#define CONTEXT_PTR_ACCESS _mind_this

#define PRIVATE PRIVATE_0(COMPONENT_NAME)
#define PRIVATE_0(compName) PRIVATE_1(compName)
#define PRIVATE_1(compName) __component_##compName##_private_data

#define INTERFACE_METHOD(itfName, methName) INTERFACE_METHOD_0(COMPONENT_NAME, itfName, methName)
#define INTERFACE_METHOD_0(compName, itfName, methName) INTERFACE_METHOD_1(compName, itfName, methName)
#define INTERFACE_METHOD_1(compName, itfName, methName) __component_##compName##_##itfName##_##methName

#define CONSTRUCTOR_METHOD CONSTRUCTOR_METHOD_0(COMPONENT_NAME)
#define CONSTRUCTOR_METHOD_0(compName) CONSTRUCTOR_METHOD_1(compName)
#define CONSTRUCTOR_METHOD_1(compName) __component_##compName##_constructor

#define DESTRUCTOR_METHOD DESTRUCTOR_METHOD_0(COMPONENT_NAME)
#define DESTRUCTOR_METHOD_0(compName) DESTRUCTOR_METHOD_1(compName)
#define DESTRUCTOR_METHOD_1(compName) __component_##compName##_destructor

#define PRIVATE_METHOD(methName) PRIVATE_METHOD_0(COMPONENT_NAME, methName)
#define PRIVATE_METHOD_0(compName, methName) PRIVATE_METHOD_1(compName, methName)
#define PRIVATE_METHOD_1(compName, methName) __component_##compName##_private_##methName

#ifdef SINGLETON
#define CALL_INTERFACE_METHOD_WITH_PARAM(itfName, methName) INTERFACE_METHOD(itfName, methName) (
#define CALL_INTERFACE_METHOD_WITHOUT_PARAM(itfName, methName) INTERFACE_METHOD(itfName, methName) ()
#else
#define CALL_INTERFACE_METHOD_WITH_PARAM(itfName, methName) INTERFACE_METHOD(itfName, methName) (CONTEXT_PTR_ACCESS,
#define CALL_INTERFACE_METHOD_WITHOUT_PARAM(itfName, methName) INTERFACE_METHOD(itfName, methName) (CONTEXT_PTR_ACCESS)
#endif

// CALL_COLLECTION_INTERFACE is not supported in MPP tests
//#define CALL_COLLECTION_INTERFACE_METHOD(itfName, index, methName) CALL_COLLECTION_INTERFACE_METHOD_0(COMPONENT_NAME, itfName, index, methName)
//#define CALL_COLLECTION_INTERFACE_METHOD_0(compName, itfName, index, methName) CALL_COLLECTION_INTERFACE_METHOD_1(compName, itfName, index, methName)
//#define CALL_COLLECTION_INTERFACE_METHOD_1(compName, itfName, index, methName) __component_##compName##_##itfName##Desc.methDesc[index].methName
//#define CALL_COLLECTION_INTERFACE_METHOD_1(compName, itfName, index, methName) __component_##comp_name##_##itfName##_##methName

#ifdef SINGLETON
#define CALL_PRIVATE_METHOD_WITH_PARAM(methName) PRIVATE_METHOD(methName) (
#define CALL_PRIVATE_METHOD_WITHOUT_PARAM(methName) PRIVATE_METHOD(methName) ()
#else
#define CALL_PRIVATE_METHOD_WITH_PARAM(methName) PRIVATE_METHOD(methName) (CONTEXT_PTR_ACCESS,
#define CALL_PRIVATE_METHOD_WITHOUT_PARAM(methName) PRIVATE_METHOD(methName) (CONTEXT_PTR_ACCESS)
#endif

#ifdef SINGLETON
#define CALL_METHOD_PTR_WITH_PARAM(f) (f) (
#define CALL_METHOD_PTR_WITHOUT_PARAM(f) (f) ()
#else
#define CALL_METHOD_PTR_WITH_PARAM(f) (f) (CONTEXT_PTR_ACCESS,
#define CALL_METHOD_PTR_WITHOUT_PARAM(f) (f) (CONTEXT_PTR_ACCESS)
#endif

#define PARAMS_RPARENT )

#define METH_PTR_DECL(ptrName) ptrName // METH_PTR_DECL_0(COMPONENT_NAME, ptrName)
#define METH_PTR_DECL_0(compName, ptrName) METH_PTR_DECL_1(compName, ptrName)
#define METH_PTR_DECL_1(compName, ptrName) (* __component_##compName##_methptr_##ptrName)

#define METH_PTR(ptrName) ptrName

#ifndef _METH_PTR_DECL
#define _METH_PTR_DECL(ptrName) (* ptrName)
#endif

#ifndef _METH_PTR
#define _METH_PTR(ptrName) ptrName
#endif


#endif //MACRO_DEF_H
