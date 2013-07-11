package test.org.jboss.forge.furnace;

import java.util.Set;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.services.ExportedInstance;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.org.jboss.forge.furnace.mocks.AbstractImplementation;
import test.org.jboss.forge.furnace.mocks.ExportedAbstractClass;
import test.org.jboss.forge.furnace.mocks.ExportedConcreteClass;
import test.org.jboss.forge.furnace.mocks.ExportedInterface;
import test.org.jboss.forge.furnace.mocks.ImplementingClass1;
import test.org.jboss.forge.furnace.mocks.ImplementingClass2;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class ContainerServiceDetectionTest
{
   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.furnace:container-cdi", version = "2.0.0-SNAPSHOT")
   })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addClasses(ExportedAbstractClass.class,
                        ExportedConcreteClass.class,
                        AbstractImplementation.class,
                        ExportedInterface.class,
                        ImplementingClass1.class,
                        ImplementingClass2.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace:container-cdi", "2.0.0-SNAPSHOT")
               )
               .addBeansXML();

      return archive;
   }

   @Inject
   private AddonRegistry registry;

   @Test
   public void testRegisteredServices()
   {
      Assert.assertNotNull(registry.getExportedInstance(ExportedConcreteClass.class));
      Assert.assertNull(registry.getExportedInstance(ExportedAbstractClass.class));

      Assert.assertNotNull(registry.getExportedInstance(ImplementingClass1.class));
      Assert.assertNotNull(registry.getExportedInstance(ImplementingClass2.class));
   }

   @Test
   public void testGetExportedInstances()
   {
      Assert.assertNotNull(registry.getExportedInstance(ExportedConcreteClass.class).get());
      Assert.assertNotNull(registry.getExportedInstance(ImplementingClass1.class).get());
      Assert.assertNotNull(registry.getExportedInstance(ImplementingClass2.class).get());
   }

   @Test(expected = Exception.class)
   public void testGetExportedInstanceBySharedAbstractClass()
   {
      registry.getExportedInstance(AbstractImplementation.class);
   }

   @Test(expected = Exception.class)
   public void testGetExportedInstanceBySharedInterface()
   {
      registry.getExportedInstance(ExportedInterface.class);
   }

   @Test
   public void testGetExportedInstancesByBaseType()
   {
      Set<ExportedInstance<ExportedInterface>> byInterface = registry.getExportedInstances(ExportedInterface.class);
      Set<ExportedInstance<AbstractImplementation>> byAbstractBaseClass = registry
               .getExportedInstances(AbstractImplementation.class);

      Assert.assertEquals(2, byInterface.size());
      Assert.assertEquals(2, byAbstractBaseClass.size());

      for (ExportedInstance<ExportedInterface> instance : byInterface)
      {
         instance.get();
      }

      for (ExportedInstance<AbstractImplementation> instance : byAbstractBaseClass)
      {
         instance.get();
      }
   }
}