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

#include "CCdelegate.h"
#include "fractal/api/BindingController.itf.h"
#include <string.h>

#define ITF_PTR(offset) ((void **) (((intptr_t)component_ptr) + offset))

static fractal_api_Component* findSubComponentSlot(
    fractal_api_Component subComponent,
    struct __component_ContentDescriptor *desc) {
  unsigned int i;

  for (i = 0; i < desc->nbStaticSubComponent; i++) {
    if (desc->staticSubComponents[i] == subComponent)
      return &(desc->staticSubComponents[i]);
  }
  for (i = 0; i < desc->nbDynamicSubComponent; i++) {
    if (desc->dynamicSubComponents[i] == subComponent)
      return &(desc->dynamicSubComponents[i]);
  }

  return NULL;
}

int __component_getFcSubComponents(fractal_api_Component subComponents[],
    struct __component_ContentDescriptor *desc) {
  unsigned int i, j;

  if (subComponents != NULL) {
    for (i = 0; i < desc->nbStaticSubComponent; i++) {
      subComponents[i] = desc->staticSubComponents[i];
    }
  } else {
    i = desc->nbStaticSubComponent;
  }
  for (j = 0; j < desc->nbDynamicSubComponent; j++) {
    if (desc->dynamicSubComponents[j] != NULL) {
      if (subComponents != NULL) {
        subComponents[i] = desc->dynamicSubComponents[j];
      }
      i++;
    }
  }

  return i;
}

int __component_addFcSubComponents(fractal_api_Component subComponent,
    struct __component_ContentDescriptor *desc) {
  unsigned int i;

  if (subComponent == NULL) {
    return FRACTAL_API_INVALID_ARG;
  }

  // first check if 'subComponent' is not already a subComponent
  if (findSubComponentSlot(subComponent, desc) != NULL) {
    return FRACTAL_API_ILLEGAL_CONTENT;
  }

  // then add it in dynamicSubComponents (if space is available)
  for (i = 0; i < desc->nbDynamicSubComponent; i++) {
    if (desc->dynamicSubComponents[i] == NULL) {
      desc->dynamicSubComponents[i] = subComponent;
      return FRACTAL_API_OK;
    }
  }

  // no space in dynamicSubComponents, try in staticSubComponents
  for (i = 0; i < desc->nbStaticSubComponent; i++) {
    if (desc->staticSubComponents[i] == NULL) {
      desc->staticSubComponents[i] = subComponent;
      return FRACTAL_API_OK;
    }
  }

  // no space available to store subComponent
  return FRACTAL_API_ILLEGAL_CONTENT;
}

int __component_removeFcSubComponents(fractal_api_Component subComponent,
    struct __component_ContentDescriptor *desc) {
  fractal_api_Component *slot;

  if (subComponent == NULL) {
    return FRACTAL_API_INVALID_ARG;
  }

  slot = findSubComponentSlot(subComponent, desc);
  if (slot == NULL) {
    // 'subComponent' is not a known sub-component.
    return FRACTAL_API_NO_SUCH_SUB_COMPONENT;
  } else {

    // TODO check that subComp is not bound.
    *slot = NULL;
    return FRACTAL_API_OK;
  }
}

int __component_addFcSubBinding(fractal_api_Component clientComponent,
      const char *clientItfName, fractal_api_Component serverComponent,
      const char *serverItfName,
      struct __component_ContentDescriptor *desc,
      void* component_ptr) {
  void *serverItf = NULL;
  void *clientBC;
  int i, err;

  if ( clientItfName == NULL || serverItfName == NULL) {
    return FRACTAL_API_INVALID_ARG;
  }

  // retrieve the server interface
  if (serverComponent == NULL || serverComponent == component_ptr) {
    // server interface is an internal interface of this composite
    for (i = 0; i < desc->internalItfsDesc->nbServerInterface; i ++) {
      if (strcmp(desc->internalItfsDesc->serverInterfaces[i].name, serverItfName) == 0) {
        serverItf = ITF_PTR(desc->internalItfsDesc->serverInterfaces[i].offset);
        break;
      }
    }
    if (serverItf == NULL) {
      // internal server interface not found
      return FRACTAL_API_ILLEGAL_BINDING;
    }

  } else {
    // server interface is an interface of a sub-component

    if (findSubComponentSlot(serverComponent, desc) == NULL) {
      // 'serverComponent' is not a known sub-component.
      return FRACTAL_API_NO_SUCH_SUB_COMPONENT;
    }

    // retrieve server interface using Component controller of sub-component
    err = serverComponent->meths->getFcInterface(serverComponent->selfData,
        serverItfName, &serverItf);
    if (err != FRACTAL_API_OK) {
      return FRACTAL_API_ILLEGAL_BINDING;
    }
  }

  if (clientComponent == NULL || clientComponent == component_ptr) {
    // client interface is an internal interface of this composite
    struct __component_InternalClientItfDescriptor *clientItf = NULL;
    for (i = 0; i < desc->internalItfsDesc->nbClientInterface; i ++) {
      if (strcmp(desc->internalItfsDesc->clientInterfaces[i].name, clientItfName) == 0) {
        clientItf = &(desc->internalItfsDesc->clientInterfaces[i]);
        break;
      }
    }
    if (clientItf == NULL) {
      // internal client interface not found
      return FRACTAL_API_ILLEGAL_BINDING;
    }

    // bind internal client interface
    *ITF_PTR(clientItf->offset) = serverItf;
    *ITF_PTR(clientItf->isBoundOffset) = serverItf;

  } else {
    // client interface is an interface of a sub-component

    // retreive client binding controller
    clientComponent->meths->getFcInterface(clientComponent->selfData,
          "bindingController", &clientBC);
    if (err != FRACTAL_API_OK) {
      return FRACTAL_API_ILLEGAL_BINDING;
    }

    // call client binding controller
    err = ((fractal_api_BindingController) clientBC)->meths->bindFc(
        ((fractal_api_BindingController) clientBC)->selfData,
        clientItfName, serverItf);
    if (err != FRACTAL_API_OK) {
      return FRACTAL_API_ILLEGAL_BINDING;
    }
  }

  return FRACTAL_API_OK;
}

int __component_removeFcSubBinding(fractal_api_Component clientComponent,
      const char *clientItfName,
      struct __component_ContentDescriptor *desc,
      void* component_ptr) {
  void *clientBC;
  int err, i;

  if (clientItfName == NULL) {
    return FRACTAL_API_INVALID_ARG;
  }

  if (clientComponent == NULL || clientComponent == component_ptr) {
    // client interface is an internal interface of this composite
    struct __component_InternalClientItfDescriptor *clientItf = NULL;
    for (i = 0; i < desc->internalItfsDesc->nbClientInterface; i ++) {
      if (strcmp(desc->internalItfsDesc->clientInterfaces[i].name, clientItfName) == 0) {
        clientItf = &(desc->internalItfsDesc->clientInterfaces[i]);
        break;
      }
    }
    if (clientItf == NULL) {
      // internal client interface not found
      return FRACTAL_API_ILLEGAL_BINDING;
    }

    // unbind internal client interface
    *ITF_PTR(clientItf->offset) = NULL;
    *ITF_PTR(clientItf->isBoundOffset) = NULL;

  } else {
    // client interface is an interface of a sub-component
    if (findSubComponentSlot(clientComponent, desc) == NULL) {
      // 'subComponent' is not a known sub-component.
      return FRACTAL_API_NO_SUCH_SUB_COMPONENT;
    }

    // retreive client binding controller
    clientComponent->meths->getFcInterface(clientComponent->selfData,
          "bindingController", &clientBC);
    if (err != FRACTAL_API_OK) {
      return FRACTAL_API_ILLEGAL_BINDING;
    }

    // call client binding controller
    err = ((fractal_api_BindingController) clientBC)->meths->unbindFc(
        ((fractal_api_BindingController) clientBC)->selfData,
        clientItfName);
    if (err != FRACTAL_API_OK) {
      return FRACTAL_API_ILLEGAL_BINDING;
    }
  }

  return FRACTAL_API_OK;
}


