package net.coding.program.setting

import android.os.Bundle
import net.coding.program.R
import net.coding.program.common.ui.KBackActivity
import net.coding.program.project.ProjectFragment
import net.coding.program.project.ProjectFragment_

class MyCreateProjectListActivity : KBackActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_created)
        initMyCreateProjectListActivity()
    }

    private fun initMyCreateProjectListActivity() {
        val fragment = ProjectFragment_()
        val args = Bundle()
        args.putSerializable("type", ProjectFragment.Type.Create)

        val extra = getIntent().getExtras()
        if (extra != null) {
            val bundle = extra.getSerializable("extra")
            if (bundle is ProjectFragment.ProjectType) {
                args.putSerializable("projectType", bundle)

                if (bundle == ProjectFragment.ProjectType.Public) {
                    setActionBarTitle("公开项目")
                } else if (bundle == ProjectFragment.ProjectType.Private) {
                    setActionBarTitle("私有项目")
                }
            }
        }
        fragment.arguments = args

        supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment)
                .commit()
    }
}
