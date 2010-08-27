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

#define CONSTRUCTOR_METHOD void CONSTRUCTOR_METHOD_0(COMPONENT_NAME) NO_PARAM_DECL
#define CONSTRUCTOR_METHOD_0(compName) CONSTRUCTOR_METHOD_1(compName)
#define CONSTRUCTOR_METHOD_1(compName) __component_##compName##_constructor

#define DESTRUCTOR_METHOD void DESTRUCTOR_METHOD_0(COMPONENT_NAME) NO_PARAM_DECL
#define DESTRUCTOR_METHOD_0(compName) DESTRUCTOR_METHOD_1(compName)
#define DESTRUCTOR_METHOD_1(compName) __component_##compName##_destructor

#define PRIVATE_METHOD(methName) PRIVATE_METHOD_0(COMPONENT_NAME, methName)
#define PRIVATE_METHOD_0(compName, methName) PRIVATE_METHOD_1(compName, methName)
#define PRIVATE_METHOD_1(compName, methName) __component_##compName##_private_##methName

#ifdef SINGLETON
#define PARAM_DECL_BEGIN (
#define PARAM_DECL_END )
#define NO_PARAM_DECL (void)
#else
#define PARAM_DECL_BEGIN ( CONTEXT_PTR_DECL,
#define PARAM_DECL_END )
#define NO_PARAM_DECL (CONTEXT_PTR_DECL)
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

#endif //MACRO_DEF_H
