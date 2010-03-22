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

#include "BCdelegate.h"
#include <string.h>

#define ITF_PTR(offset) ((void **) (((intptr_t)component_ptr) + offset))

int __component_listFc_delegate(const char *clientItfNames[],
    struct __component_BindingDescriptors *desc)
{
  unsigned int i;

  if (clientItfNames != NULL) {
    for (i = 0; i < desc->nbBindings; i++) {
      clientItfNames[i] = desc->bindingDesc[i].name;
    }
  }
  return desc->nbBindings;
}

int __component_lookupFc_delegate(const char *clientItfName,
    void **interfaceReference, struct __component_BindingDescriptors *desc,
    void* component_ptr)
{
  unsigned int i;

  if (interfaceReference == NULL || clientItfName == NULL) {
    return FRACTAL_API_INVALID_ARG;
  }

  for (i = 0; i < desc->nbBindings; i++) {
    if (strcmp(desc->bindingDesc[i].name, clientItfName) == 0) {
      *interfaceReference = *(ITF_PTR(desc->bindingDesc[i].offset));
      return FRACTAL_API_OK;
    }
  }
  return FRACTAL_API_NO_SUCH_INTERFACE;
}

int __component_bindFc_delegate(const char *clientItfName, void *serverItf,
    struct __component_BindingDescriptors *desc, void* component_ptr)
{
  unsigned int i;

  if (clientItfName == NULL) {
    return FRACTAL_API_INVALID_ARG;
  }

  for (i = 0; i < desc->nbBindings; i++) {
    if (strcmp(desc->bindingDesc[i].name, clientItfName) == 0) {
      *ITF_PTR(desc->bindingDesc[i].offset) = serverItf;
      return FRACTAL_API_OK;
    }
  }
  return FRACTAL_API_NO_SUCH_INTERFACE;
}

int __component_unbindFc_delegate(const char *clientItfName,
    struct __component_BindingDescriptors *desc, void* component_ptr)
{
  return __component_bindFc_delegate(clientItfName, NULL, desc, component_ptr);
}
