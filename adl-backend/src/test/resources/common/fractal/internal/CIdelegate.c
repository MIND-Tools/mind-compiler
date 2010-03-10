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

#include "CIdelegate.h"
#include <string.h>

#define ITF_PTR(offset) ((void *) (((intptr_t)component_ptr) + offset))

int __component_getFcInterface_delegate(const char *interfaceName,
    void **interfaceReference, struct __component_InterfaceDescriptors *desc,
    void* component_ptr)
{
  unsigned int i;

  /* check argument */
  if (interfaceReference == NULL) {
    return FRACTAL_API_INVALID_ARG;
  }

  /* search an interface with the correct name. */
  for (i = 0; i < desc->nbItfs; i++) {
    if (strcmp(desc->itfDesc[i].name, interfaceName) == 0) {
      *interfaceReference = ITF_PTR(desc->itfDesc[i].offset);
      return FRACTAL_API_OK;
    }
  }
  return FRACTAL_API_NO_SUCH_INTERFACE;
}

int __component_getFcInterfaces_delegate(void* interfaceReferences[],
    struct __component_InterfaceDescriptors *desc, void* component_ptr)
{
  unsigned int i;

  if (interfaceReferences != NULL) {
    for (i = 0; i < desc->nbItfs; i++) {
      interfaceReferences[i] = ITF_PTR(desc->itfDesc[i].offset);
    }
  }
  return desc->nbItfs;
}

int __component_listFcInterfaces_delegate(const char* interfaceNames[],
    struct __component_InterfaceDescriptors *desc)
{
  unsigned int i;

  if (interfaceNames != NULL) {
    for (i = 0; i < desc->nbItfs; i++) {
      interfaceNames[i] = desc->itfDesc[i].name;
    }
  }
  return desc->nbItfs;
}

int __component_getFcInterfaceRole_delegate(const char* interfaceName,
    struct __component_InterfaceDescriptors *desc)
{
  unsigned int i;

  /* search an interface with the correct name. */
  for (i = 0; i < desc->nbItfs; i++) {
    if (strcmp(desc->itfDesc[i].name, interfaceName) == 0) {
      return desc->itfDesc[i].type;
    }
  }
  return FRACTAL_API_NO_SUCH_INTERFACE;
}
