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

#include "ACdelegate.h"
#include <string.h>

#define ATTR_PTR(offset) ((void *) (((intptr_t)component_ptr) + offset))

int __component_listFcAttributes_delegate(const char *attributeNames[],
    struct __component_AttributeDescriptors *desc)
{
  unsigned int i;

  if (attributeNames != NULL) {
    for (i = 0; i < desc->nbAttributes; i++) {
      attributeNames[i] = desc->attributeDesc[i].name;
    }
  }
  return desc->nbAttributes;
}

int __component_getFcAttribute_delegate(const char *attributeName,
    void **value, struct __component_AttributeDescriptors *desc,
    void* component_ptr)
{
  unsigned int i;

  if ((attributeName == NULL) || (value == NULL)) {
    return FRACTAL_API_INVALID_ARG;
  }

  for (i = 0; i < desc->nbAttributes; i++) {
    if (strcmp(desc->attributeDesc[i].name, attributeName) == 0) {
      /* copy attribute value into the out parameter. */
      memcpy(value, ATTR_PTR(desc->attributeDesc[i].offset),
          desc->attributeDesc[i].size);
      return FRACTAL_API_OK;
    }
  }
  return FRACTAL_API_NO_SUCH_ATTRIBUTE;
}

int __component_getFcAttributeSize_delegate(const char *attributeName,
    struct __component_AttributeDescriptors *desc)
{
  unsigned int i;

  if (attributeName == NULL) {
    return FRACTAL_API_INVALID_ARG;
  }

  for (i = 0; i < desc->nbAttributes; i++) {
    if (strcmp(desc->attributeDesc[i].name, attributeName) == 0) {
      return desc->attributeDesc[i].size;
    }
  }
  return FRACTAL_API_NO_SUCH_ATTRIBUTE;
}

int __component_setFcAttribute_delegate(const char *attributeName, void *value,
    struct __component_AttributeDescriptors *desc, void* component_ptr)
{
  unsigned int i;

  if (attributeName == NULL) {
    return FRACTAL_API_INVALID_ARG;
  }

  for (i = 0; i < desc->nbAttributes; i++) {
    if (strcmp(desc->attributeDesc[i].name, attributeName) == 0) {
      /* copy given value into attribute value. The value of the set
       attribute is passed by value (not by reference) that why the "value"
       parameter is dereferenced (the "value" parameter does not point to the
       value to set, the "value" parameter contains the value to set). */
      memcpy(ATTR_PTR(desc->attributeDesc[i].offset), &value,
          desc->attributeDesc[i].size);
      return FRACTAL_API_OK;
    }
  }
  return FRACTAL_API_NO_SUCH_ATTRIBUTE;
}
