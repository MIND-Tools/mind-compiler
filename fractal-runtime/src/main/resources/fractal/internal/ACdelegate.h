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

#ifndef FRACTAL_INTERNAL_ACDELEGATE
#define FRACTAL_INTERNAL_ACDELEGATE

#include <stdint.h>

/* Include ErrorCode.idt directly since it contains only pure C constructs. */
#include "../api/ErrorCode.idt"

struct __component_AttributeDescriptor
{
  const char *name;
  intptr_t offset;
  int size;
};

struct __component_AttributeDescriptors
{
  int nbAttributes;
  struct __component_AttributeDescriptor attributeDesc[2];
};

#define __COMPONENT_INIT_ATTRIBUTE_DESCRIPTOR(typeName, attrName) \
  {#attrName, \
   ((intptr_t) &(((typeName *) 0x0)->__component_internal_data.attributes.attrName)), \
   sizeof(((typeName *) 0x0)->__component_internal_data.attributes.attrName)}

int __component_listFcAttributes_delegate(const char *attributeNames[],
    struct __component_AttributeDescriptors *desc);

int __component_getFcAttribute_delegate(const char *attributeName,
    void **value, struct __component_AttributeDescriptors *desc,
    void* component_ptr);

int __component_getFcAttributeSize_delegate(const char *attributeName,
    struct __component_AttributeDescriptors *desc);

int __component_setFcAttribute_delegate(const char *attributeName, void *value,
    struct __component_AttributeDescriptors *desc, void* component_ptr);

#endif
