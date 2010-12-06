/**
 * Fractal Runtime
 *
 * Copyright (C) 2009 STMicroelectronics
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 */

#ifndef FRACTAL_INTERNAL_CCDELEGATE
#define FRACTAL_INTERNAL_CCDELEGATE

#include "fractal/api/ErrorCode.idt.h"
#include "fractal/api/ContentController.itf.h"

struct __component_InternalClientItfDescriptor {
  const char *name;
  intptr_t offset;
  intptr_t isBoundOffset;
};

#define __COMPONENT_INIT_INTERNAL_CLIENT_ITF_DESCRIPTOR(typeName, itfName, delegate_itfName) \
  {#itfName, ((intptr_t) &(((typeName *) 0x0)->__component_internal_data.inner_type.itfName)), ((intptr_t) &(((typeName *) 0x0)->__component_internal_data.type.delegate_itfName.isBound))}


struct __component_InternalServerItfDescriptor {
  const char *name;
  intptr_t offset;
};

#define __COMPONENT_INIT_INTERNAL_SERVER_ITF_DESCRIPTOR(typeName, itfName) \
  {#itfName, ((intptr_t) &(((typeName *) 0x0)->__component_internal_data.inner_type.itfName))}

#define __COMPONENT_INIT_CONTROLLER_SERVER_ITF_DESCRIPTOR(typeName, itfName) \
  {#itfName, ((intptr_t) &(((typeName *) 0x0)->__component_internal_data.type.itfName))}

struct __component_InternalItfsDescriptor {
  int nbClientInterface;
  struct __component_InternalClientItfDescriptor *clientInterfaces;
  int nbServerInterface;
  struct __component_InternalServerItfDescriptor *serverInterfaces;
};

struct __component_SubComponentDescriptor {
  fractal_api_Component subComponent;
  __MIND_STRING_TYPEDEF name;
};

struct __component_ContentDescriptor
{
  int nbSubComponent;
  struct __component_SubComponentDescriptor *subComponents;
  struct __component_InternalItfsDescriptor *internalItfsDesc;
};

#define __COMPONENT_STRINGIFY_ITF_NAME(itfName) #itfName

int __component_getFcSubComponents(fractal_api_Component subComponents[],
    struct __component_ContentDescriptor *desc);

int __component_getFcSubComponent(__MIND_STRING_TYPEDEF name,
    fractal_api_Component *subComponent,
    struct __component_ContentDescriptor *desc);

int __component_getFcSubComponentName(fractal_api_Component subComponent,
    __MIND_STRING_TYPEDEF *name,
    struct __component_ContentDescriptor *desc);

int __component_addFcSubComponent(fractal_api_Component subComponent,
    struct __component_ContentDescriptor *desc);

int __component_addFcNamedSubComponent(fractal_api_Component subComponent,
    __MIND_STRING_TYPEDEF name,
    struct __component_ContentDescriptor *desc);

int __component_removeFcSubComponents(fractal_api_Component subComponent,
    struct __component_ContentDescriptor *desc);

int __component_addFcSubBinding(fractal_api_Component clientComponent,
    __MIND_STRING_TYPEDEF clientItfName, fractal_api_Component serverComponent,
    __MIND_STRING_TYPEDEF serverItfName,
    struct __component_ContentDescriptor *desc,
    void* component_ptr);

int __component_removeFcSubBinding(fractal_api_Component clientComponent,
    __MIND_STRING_TYPEDEF clientItfName,
    struct __component_ContentDescriptor *desc,
    void* component_ptr);

#endif
