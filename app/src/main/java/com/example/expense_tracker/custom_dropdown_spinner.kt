import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.expense_tracker.R

class CustomSpinnerAdapter(
    context: Context,
    resource: Int,
    objects: Array<String>
) : ArrayAdapter<String>(context, resource, objects) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val resource: Int = resource
    private val objects: Array<String> = objects

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(resource, parent, false)
        val textView = view.findViewById<TextView>(R.id.dropdown_item_text)

        // Ensure textView is not null
        textView?.text = objects[position]

        return view
    }
}
