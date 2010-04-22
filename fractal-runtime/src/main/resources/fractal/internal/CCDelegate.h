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

struct __component_ContentBindingDescriptor {
  fractal_api_Component clientComponent;
  const char* clientItfName;
  fractal_api_Component serverComponent;
  const char* serverItfName;
};

struct __component_ContentDescriptor
{
  int nbStaticSubComponent;
  fractal_api_Component *staticSubComponents;
  int nbDynamicSubComponent;
  fractal_api_Component *dynamicSubComponents;
  int nbBindingDescriptor;
  struct __component_ContentBindingDescriptor *bindingDesc;
};

#define __COMPONENT_STRINGIFY_ITF_NAME(itfName) #itfName

int __component_getFcSubComponents(fractal_api_Component subComponents[],
    struct __component_ContentDescriptor *desc);

int __component_addFcSubComponents(fractal_api_Component subComponent,
    struct __component_ContentDescriptor *desc);

int __component_removeFcSubComponents(fractal_api_Component subComponent,
    struct __component_ContentDescriptor *desc);

int __component_addFcSubBinding(fractal_api_Component clientComponent,
      const char *clientItfName, fractal_api_Component serverComponent,
      const char *serverItfName,
      struct __component_ContentDescriptor *desc);

int __component_removeFcSubBinding(fractal_api_Component clientComponent,
      const char *clientItfName,
      struct __component_ContentDescriptor *desc);

#endif
