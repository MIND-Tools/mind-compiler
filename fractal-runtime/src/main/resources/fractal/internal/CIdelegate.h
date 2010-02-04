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

#ifndef FRACTAL_INTERNAL_CIDELEGATE
#define FRACTAL_INTERNAL_CIDELEGATE

#include <stdint.h>

/* Include ErrorCode.idt directly since it contains only pure C constructs. */
#include "../api/ErrorCode.idt"

struct __component_InterfaceDescriptor
{
  const char *name;
  intptr_t offset;
  int type;
};

struct __component_InterfaceDescriptors
{
  int nbItfs;
  struct __component_InterfaceDescriptor itfDesc[2];
};

#define __COMPONENT_INIT_CLIENT_INTERFACE_DESCRIPTOR(typeName, itfName) \
  {#itfName, ((intptr_t) &(((typeName *) 0x0)->__component_internal_data.type.itfName)), 0}

#define __COMPONENT_INIT_SERVER_INTERFACE_DESCRIPTOR(typeName, itfName) \
  {#itfName, ((intptr_t) &(((typeName *) 0x0)->__component_internal_data.type.itfName)), 1}

int __component_getFcInterface_delegate(const char *interfaceName,
    void **interfaceReference, struct __component_InterfaceDescriptors *desc,
    void* component_ptr);

int __component_getFcInterfaces_delegate(void* interfaceReferences[],
    struct __component_InterfaceDescriptors *desc, void* component_ptr);

int __component_listFcInterfaces_delegate(const char* interfaceNames[],
    struct __component_InterfaceDescriptors *desc);

int __component_getFcInterfaceRole_delegate(const char* interfaceName,
    struct __component_InterfaceDescriptors *desc);

#endif
